package m3.dominio.excepcion;

import java.util.UUID;

/**
 * Thrown when a driver does not have the required qualification
 * (approved status + own vehicle) to request availability.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class ConductorNoHabilitadoExcepcion extends RuntimeException {

    private final UUID driverId;
    private final String reason;

    public ConductorNoHabilitadoExcepcion(UUID driverId, String reason) {
        super("Conductor " + driverId + " is not qualified to request availability: " + reason);
        this.driverId = driverId;
        this.reason = reason;
    }

    public UUID getDriverId() { return driverId; }
    public String getReason() { return reason; }
}
