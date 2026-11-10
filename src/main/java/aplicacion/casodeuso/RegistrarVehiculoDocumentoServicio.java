package m3.aplicacion.casodeuso;

import m3.aplicacion.puerto.DocumentoRepositorio;
import m3.aplicacion.puerto.UnidadDeTrabajo;
import m3.aplicacion.puerto.VehiculoRepositorio;
import m3.dominio.Documento;
import m3.dominio.Vehiculo;
import m3.dominio.excepcion.VehiculoProhibidoExcepcion;
import m3.dominio.excepcion.VehiculoNoEncontradoExcepcion;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service implementing RegistrarVehiculoDocumentoUsoCaso (HU-06 / RF-3.3).
 * Operates purely on domain models and ports, executed within UnidadDeTrabajo transactional boundaries.
 */
public class RegistrarVehiculoDocumentoServicio implements RegistrarVehiculoDocumentoUsoCaso {

    private final DocumentoRepositorio documentRepository;
    private final VehiculoRepositorio vehicleRepository;
    private final UnidadDeTrabajo unitOfWork;
    private final m3.aplicacion.puerto.ConductorRepositorio drivers;

    public RegistrarVehiculoDocumentoServicio(
        DocumentoRepositorio documentRepository,
        VehiculoRepositorio vehicleRepository,
        UnidadDeTrabajo unitOfWork
    ) {
        this(documentRepository,vehicleRepository,unitOfWork,null);
    }
    public RegistrarVehiculoDocumentoServicio(DocumentoRepositorio documentRepository,VehiculoRepositorio vehicleRepository,UnidadDeTrabajo unitOfWork,
            m3.aplicacion.puerto.ConductorRepositorio drivers) {
        this.drivers=drivers;
        this.documentRepository = Objects.requireNonNull(documentRepository, "documentRepository cannot be null");
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "vehicleRepository cannot be null");
        this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork cannot be null");
    }

    @Override
    public Documento registerVehicleDocument(RegistrarVehiculoDocumentoComando command) {
        Objects.requireNonNull(command, "command cannot be null");

        return unitOfWork.executeInTransaction(() -> {
            var driver=drivers==null ? null : drivers.findByIdWithPessimisticLock(command.driverId())
                .orElseThrow(() -> new m3.dominio.excepcion.ConductorNoEncontradoExcepcion(command.driverId()));
            // 1. Verify vehicle existence (404 if not found)
            Vehiculo vehicle = vehicleRepository.findById(command.vehicleId())
                .orElseThrow(() -> new VehiculoNoEncontradoExcepcion(command.vehicleId()));

            // 2. Verify vehicle ownership (403 if belonging to another driver)
            if (!vehicle.belongsToDriver(command.driverId())) {
                throw new VehiculoProhibidoExcepcion(command.driverId(), command.vehicleId());
            }

            // 3. Find existing current document for this vehicle and type
            Optional<Documento> existingCurrent = documentRepository.findCurrentByVehicleIdAndType(
                command.vehicleId(),
                command.documentType()
            );

            // 4. Calculate next revision number
            int nextRevision = existingCurrent.map(doc -> doc.getRevision() + 1).orElse(1);

            // 5. Non-destructive versioning: Mark previous current revision as non-current
            existingCurrent.ifPresent(previous -> {
                previous.markAsNonCurrent();
                documentRepository.save(previous);
            });

            // 6. Instantiate new document revision (domain model validates issuedAt < validUntil, sets isCurrent=true, status=PENDING)
            Documento newDocument = Documento.createPendingRevision(
                command.driverId(),
                command.vehicleId(),
                command.documentType(),
                command.documentNumber(),
                command.issuingEntity(),
                command.issuedAt(),
                command.validUntil(),
                command.storageReference(),
                nextRevision
            );

            if(driver!=null) {
                driver.incrementVersion(); drivers.save(driver); newDocument.assignDriverVersion(driver.getVersion());
                if("OBSERVED".equals(vehicle.getQualificationStatus()) || "REJECTED".equals(vehicle.getQualificationStatus()))
                    vehicleRepository.save(vehicle.withQualification(m3.dominio.HabilitacionEstado.PENDING,driver.getVersion()));
            }
            // 7. Persist and return
            return documentRepository.save(newDocument);
        });
    }

    @Override
    public List<Documento> listVehicleDocuments(UUID driverId, UUID vehicleId) {
        Objects.requireNonNull(driverId, "driverId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");

        Vehiculo vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new VehiculoNoEncontradoExcepcion(vehicleId));

        if (!vehicle.belongsToDriver(driverId)) {
            throw new VehiculoProhibidoExcepcion(driverId, vehicleId);
        }

        return documentRepository.findByVehicleIdOrderByRevisionDesc(vehicleId);
    }
}

