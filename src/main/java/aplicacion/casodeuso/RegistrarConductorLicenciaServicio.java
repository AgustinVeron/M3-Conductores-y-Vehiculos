package m3.aplicacion.casodeuso;

import m3.aplicacion.puerto.DocumentoRepositorio;
import m3.aplicacion.puerto.ConductorRepositorio;
import m3.aplicacion.puerto.IdempotenciaRepositorio;
import m3.aplicacion.puerto.UnidadDeTrabajo;
import m3.dominio.Documento;
import m3.dominio.Conductor;
import m3.dominio.IdempotenciaRegistro;
import m3.dominio.excepcion.ConductorNoEncontradoExcepcion;
import m3.dominio.excepcion.DocumentoNoEncontradoExcepcion;
import m3.dominio.excepcion.IdempotenciaConflictoExcepcion;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RegistrarConductorLicenciaServicio implements RegistrarConductorLicenciaUsoCaso {
    private final ConductorRepositorio drivers;
    private final DocumentoRepositorio documents;
    private final UnidadDeTrabajo unitOfWork;
    private final IdempotenciaRepositorio idempotency;

    public RegistrarConductorLicenciaServicio(ConductorRepositorio drivers, DocumentoRepositorio documents, UnidadDeTrabajo unitOfWork) {
        this(drivers, documents, unitOfWork, null);
    }

    public RegistrarConductorLicenciaServicio(ConductorRepositorio drivers, DocumentoRepositorio documents,
                                        UnidadDeTrabajo unitOfWork, IdempotenciaRepositorio idempotency) {
        this.drivers = Objects.requireNonNull(drivers);
        this.documents = Objects.requireNonNull(documents);
        this.unitOfWork = Objects.requireNonNull(unitOfWork);
        this.idempotency = idempotency;
    }

    @Override
    public Documento register(RegistrarConductorLicenciaComando command) {
        Objects.requireNonNull(command);
        return unitOfWork.executeInTransaction(() -> {
            // The driver lock serializes concurrent renewals and protects the revision number.
            Conductor driver = drivers.findByIdWithPessimisticLock(command.driverId())
                    .orElseThrow(() -> new ConductorNoEncontradoExcepcion(command.driverId()));
            String path = "/api/v1/drivers/" + driver.getId() + "/documents";
            if (command.idempotencyKey() != null) {
                IdempotenciaRegistro existing = idempotency.findByKeyAndMethodAndPath(
                        command.idempotencyKey(), "POST", path).orElse(null);
                if (existing != null) {
                    if (!existing.getRequestHash().equals(command.requestHash())) {
                        throw new IdempotenciaConflictoExcepcion("Idempotency-Key was reused with different license data");
                    }
                    UUID documentId = UUID.fromString(existing.getResponseBody());
                    return documents.findById(documentId)
                            .orElseThrow(() -> new DocumentoNoEncontradoExcepcion(documentId));
                }
            }
            driver.validateVersion(command.expectedVersion());

            Documento previous = documents.findCurrentLicenseByDriverId(driver.getId()).orElse(null);
            int revision = previous == null
                    ? documents.findLicensesByDriverId(driver.getId()).stream()
                            .mapToInt(Documento::getRevision).max().orElse(0) + 1
                    : previous.getRevision() + 1;
            Documento next = Documento.createPendingLicense(driver.getId(), command.documentNumber(),
                    command.issuedAt(), command.validUntil(), command.serviceTypes(),
                    command.storageReference(), revision, driver.getVersion() + 1);

            if (previous != null) {
                previous.markAsNonCurrent();
                documents.save(previous);
            }
            if (driver.getQualificationStatus()==m3.dominio.HabilitacionEstado.OBSERVED || driver.getQualificationStatus()==m3.dominio.HabilitacionEstado.REJECTED)
                driver.changeQualificationStatus(m3.dominio.HabilitacionEstado.PENDING);
            driver.incrementVersion();
            drivers.save(driver);
            Documento created = documents.save(next);
            if (command.idempotencyKey() != null) {
                idempotency.save(new IdempotenciaRegistro(UUID.randomUUID(), command.idempotencyKey(),
                        driver.getId().toString(), "POST", path, command.requestHash(),
                        201, created.getId().toString(), Instant.now()));
            }
            return created;
        });
    }

    @Override
    public List<Documento> history(UUID driverId) {
        driver(driverId);
        return documents.findLicensesByDriverId(driverId);
    }

    @Override
    public Conductor driver(UUID driverId) {
        return drivers.findById(driverId).orElseThrow(() -> new ConductorNoEncontradoExcepcion(driverId));
    }
}
