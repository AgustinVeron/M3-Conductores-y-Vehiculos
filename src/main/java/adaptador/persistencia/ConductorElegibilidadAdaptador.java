package m3.adaptador.persistencia;

import m3.adaptador.persistencia.entidad.DocumentoJpaEntidad;
import m3.adaptador.persistencia.entidad.ConductorJpaEntidad;
import m3.adaptador.persistencia.entidad.VehiculoJpaEntidad;
import m3.adaptador.persistencia.repositorio.SpringDatosDocumentoRepositorio;
import m3.adaptador.persistencia.repositorio.SpringDatosConductorRepositorio;
import m3.adaptador.persistencia.repositorio.SpringDatosVehiculoRepositorio;
import m3.aplicacion.puerto.salida.ConductorElegibilidadPuerto;
import m3.dominio.DocumentoTipo;
import m3.dominio.HabilitacionEstado;
import m3.dominio.ServicioTipo;
import m3.dominio.elegibilidad.DocumentoInstantanea;
import m3.dominio.elegibilidad.ConductorInstantanea;
import m3.dominio.elegibilidad.VehiculoInstantanea;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class ConductorElegibilidadAdaptador implements ConductorElegibilidadPuerto {

    private final SpringDatosConductorRepositorio driverRepository;
    private final SpringDatosVehiculoRepositorio vehicleRepository;
    private final SpringDatosDocumentoRepositorio documentRepository;

    public ConductorElegibilidadAdaptador(SpringDatosConductorRepositorio driverRepository,
                                  SpringDatosVehiculoRepositorio vehicleRepository,
                                  SpringDatosDocumentoRepositorio documentRepository) {
        this.driverRepository = Objects.requireNonNull(driverRepository);
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository);
        this.documentRepository = Objects.requireNonNull(documentRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConductorInstantanea> findSnapshotById(UUID driverId) {
        return driverRepository.findById(driverId).map(this::mapToSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConductorInstantanea> findSnapshotByIdentitySubject(String identitySubject) {
        return driverRepository.findByIdentitySubject(identitySubject).map(this::mapToSnapshot);
    }

    private ConductorInstantanea mapToSnapshot(ConductorJpaEntidad driver) {
        UUID driverId = driver.getId();
        List<VehiculoJpaEntidad> vehicles = vehicleRepository.findByDriverId(driverId);

        VehiculoInstantanea selectedVehicle = null;
        if (!vehicles.isEmpty()) {
            VehiculoJpaEntidad bestVehicle = vehicles.stream()
                    .filter(v -> "APPROVED".equalsIgnoreCase(v.getQualificationStatus()))
                    .findFirst()
                    .orElse(vehicles.get(0));
            selectedVehicle = mapVehicle(bestVehicle);
        }

        DocumentoInstantanea licenseSnapshot = documentRepository
                .findByDriverIdAndDocumentTypeAndVehicleIdIsNullAndCurrentTrue(driverId, DocumentoTipo.LICENSE)
                .map(this::mapDocument)
                .orElse(null);

        HabilitacionEstado driverStatus = HabilitacionEstado.PENDING;
        if (driver.getQualificationStatus() != null) {
            try {
                driverStatus = HabilitacionEstado.valueOf(driver.getQualificationStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        int version = driver.getVersion() != null ? driver.getVersion().intValue() : 1;

        return new ConductorInstantanea(
                driver.getId(),
                driver.getIdentitySubject(),
                driver.getDisplayName(),
                driverStatus,
                selectedVehicle,
                licenseSnapshot,
                version
        );
    }

    private VehiculoInstantanea mapVehicle(VehiculoJpaEntidad vehicle) {
        DocumentoInstantanea insuranceSnapshot = documentRepository
                .findByVehicleIdAndDocumentTypeAndCurrentTrue(vehicle.getId(), DocumentoTipo.INSURANCE)
                .map(this::mapDocument)
                .orElse(null);

        Set<ServicioTipo> serviceTypes = new HashSet<>();
        if (vehicle.getServiceTypes() != null && !vehicle.getServiceTypes().isBlank()) {
            for (String s : vehicle.getServiceTypes().split(",")) {
                try {
                    serviceTypes.add(ServicioTipo.valueOf(s.trim().toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        HabilitacionEstado vehicleStatus = HabilitacionEstado.PENDING;
        if (vehicle.getQualificationStatus() != null) {
            try {
                vehicleStatus = HabilitacionEstado.valueOf(vehicle.getQualificationStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        return new VehiculoInstantanea(
                vehicle.getId(),
                vehicleStatus,
                serviceTypes,
                insuranceSnapshot,
                documentRepository.findByVehicleIdAndDocumentTypeAndCurrentTrue(vehicle.getId(),DocumentoTipo.VEHICLE_REGISTRATION).map(this::mapDocument).orElse(null)
        );
    }

    private DocumentoInstantanea mapDocument(DocumentoJpaEntidad document) {
        HabilitacionEstado docStatus = HabilitacionEstado.PENDING;
        if (document.getStatus() != null) {
            try {
                docStatus = HabilitacionEstado.valueOf(document.getStatus().name());
            } catch (IllegalArgumentException ignored) {}
        }

        return new DocumentoInstantanea(
                document.getId(),
                document.getDocumentType(),
                docStatus,
                document.getValidUntil(),document.getIssuedAt(),Set.copyOf(document.getServiceTypes())
        );
    }
}
