package m3.aplicacion.casodeuso;

import m3.dominio.Vehiculo;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para la consulta de vehículos de un conductor (HU-04 / RF-3.2).
 */
public interface ObtenerConductorVehiculosUsoCaso {

    List<Vehiculo> listVehiclesByDriver(UUID driverId);

    Vehiculo getVehicle(UUID driverId, UUID vehicleId);
}