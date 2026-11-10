package m3.dominio.excepcion;

import java.util.UUID;

/**
 * Thrown when the vehicle exists but does not belong to the requested driver (403 Forbidden).
 */
public class VehiculoProhibidoExcepcion extends RuntimeException {

    private final UUID driverId;
    private final UUID vehicleId;

    public VehiculoProhibidoExcepcion(UUID driverId, UUID vehicleId) {
        super(String.format("Vehiculo %s does not belong to driver %s", vehicleId, driverId));
        this.driverId = driverId;
        this.vehicleId = vehicleId;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }
}

