package m3.aplicacion.puerto;

import m3.dominio.Vehiculo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para consultas y validaciones de vehículos.
 */
public interface VehiculoRepositorio {

    Vehiculo save(Vehiculo vehicle);

    Optional<Vehiculo> findById(UUID vehicleId);

    List<Vehiculo> findByDriverId(UUID driverId);

    Optional<Vehiculo> findByIdAndDriverId(UUID vehicleId, UUID driverId);

    boolean existsByIdAndDriverId(UUID vehicleId, UUID driverId);
}