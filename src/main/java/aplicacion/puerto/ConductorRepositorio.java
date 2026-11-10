package m3.aplicacion.puerto;

import m3.dominio.Conductor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de repositorio para el agregado Conductor según la arquitectura hexagonal
 * de M3.
 */
public interface ConductorRepositorio {

    Optional<Conductor> findById(UUID id);

    Optional<Conductor> findByIdWithPessimisticLock(UUID id);

    Optional<Conductor> findByIdentitySubject(String identitySubject);

    boolean existsByIdentitySubject(String identitySubject);

    Conductor save(Conductor driver);

    default List<Conductor> findAll() {
        return List.of();
    }

    /**
     * Returns true if the driver exists in M3.
     */
    boolean existsById(UUID driverId);

    /**
     * Returns true if the driver has APPROVED qualification status.
     */
    boolean isApproved(UUID driverId);

    /**
     * Returns true if the given vehicle exists, is APPROVED, and belongs to the
     * driver.
     */
    boolean hasOwnQualifiedVehicle(UUID driverId, UUID vehicleId);
}
