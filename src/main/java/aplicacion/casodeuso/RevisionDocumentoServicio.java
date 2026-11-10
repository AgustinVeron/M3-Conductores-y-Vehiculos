package m3.aplicacion.casodeuso;

import m3.aplicacion.puerto.AuditoriaRegistroRepositorio;
import m3.aplicacion.puerto.DocumentoRepositorio;
import m3.aplicacion.puerto.ConductorRepositorio;
import m3.aplicacion.puerto.VehiculoRepositorio;
import m3.dominio.AuditoriaRegistro;
import m3.dominio.Documento;
import m3.dominio.Conductor;
import m3.dominio.excepcion.DocumentoNoEncontradoExcepcion;
import m3.dominio.excepcion.ConductorNoEncontradoExcepcion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class RevisionDocumentoServicio implements RevisionDocumentoUsoCaso {

    private final ConductorRepositorio driverRepository;
    private final DocumentoRepositorio documentRepository;
    private final VehiculoRepositorio vehicleRepository;
    private final AuditoriaRegistroRepositorio auditLogRepository;

    public RevisionDocumentoServicio(ConductorRepositorio driverRepository,
                                 DocumentoRepositorio documentRepository,
                                 VehiculoRepositorio vehicleRepository,
                                 AuditoriaRegistroRepositorio auditLogRepository) {
        this.driverRepository = Objects.requireNonNull(driverRepository, "driverRepository must not be null");
        this.documentRepository = Objects.requireNonNull(documentRepository, "documentRepository must not be null");
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "vehicleRepository must not be null");
        this.auditLogRepository = Objects.requireNonNull(auditLogRepository, "auditLogRepository must not be null");
    }

    @Override
    @Transactional
    public Documento reviewDocument(RevisionDocumentoComando command) {
        Objects.requireNonNull(command, "command must not be null");

        Conductor driver = driverRepository.findByIdWithPessimisticLock(command.driverId())
                .orElseThrow(() -> new ConductorNoEncontradoExcepcion(command.driverId()));

        driver.validateVersion(command.ifMatchVersion());

        Documento document = documentRepository.findById(command.documentId())
                .orElseThrow(() -> new DocumentoNoEncontradoExcepcion(command.documentId()));

        if (!document.getDriverId().equals(command.driverId())) {
            throw new DocumentoNoEncontradoExcepcion(command.documentId());
        }

        if (document.getVehicleId() != null
                && !vehicleRepository.existsByIdAndDriverId(document.getVehicleId(), command.driverId())) {
            throw new DocumentoNoEncontradoExcepcion(command.documentId());
        }

        String previousStatus = document.getStatus().name();
        document.validateReview(command.decision(), command.reason());

        driver.incrementVersion();
        driverRepository.save(driver);

        document.review(command.decision(), command.reason(), command.actor(), driver.getVersion());
        Documento updatedDocument = documentRepository.save(document);

        AuditoriaRegistro auditLog = new AuditoriaRegistro(
                UUID.randomUUID(),
                "DOCUMENT",
                updatedDocument.getId(),
                driver.getId(),
                "REVIEW",
                command.actor() != null && !command.actor().isBlank() ? command.actor() : "OPERATOR",
                previousStatus,
                updatedDocument.getStatus().name(),
                updatedDocument.getRejectionReason(),
                command.correlationId(),
                Instant.now()
        );
        auditLogRepository.save(auditLog);

        return updatedDocument;
    }
}
