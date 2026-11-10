package m3.aplicacion.casodeuso;

import m3.aplicacion.puerto.VehiculoRepositorio;
import m3.dominio.Vehiculo;
import m3.dominio.excepcion.VehiculoProhibidoExcepcion;
import m3.dominio.excepcion.VehiculoNoEncontradoExcepcion;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementación de la lógica de negocio para consultar vehículos (HU-04).
 */
public class ObtenerConductorVehiculosServicio implements ObtenerConductorVehiculosUsoCaso {

    private final VehiculoRepositorio vehicleRepository;

    public ObtenerConductorVehiculosServicio(VehiculoRepositorio vehicleRepository) {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "vehicleRepository cannot be null");
    }

    @Override
    public List<Vehiculo> listVehiclesByDriver(UUID driverId) {
        Objects.requireNonNull(driverId, "driverId cannot be null");
        return vehicleRepository.findByDriverId(driverId);
    }

    @Override
    public Vehiculo getVehicle(UUID driverId, UUID vehicleId) {
        Objects.requireNonNull(driverId, "driverId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");

        Vehiculo vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new VehiculoNoEncontradoExcepcion(vehicleId));

        if (!vehicle.belongsToDriver(driverId)) {
            throw new VehiculoProhibidoExcepcion(driverId, vehicleId);
        }

        return vehicle;
    }
}