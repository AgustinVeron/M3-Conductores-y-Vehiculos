package m3.dominio.excepcion;

import java.util.UUID;

/**
 * Thrown when the requested vehicle does not exist.
 */
public class VehiculoNoEncontradoExcepcion extends RuntimeException {

    private final UUID vehicleId;

    public VehiculoNoEncontradoExcepcion(UUID vehicleId) {
        super(String.format("Vehiculo with ID %s not found", vehicleId));
        this.vehicleId = vehicleId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }
}

