package m3.dominio.excepcion;

import java.util.UUID;

/**
 * Thrown when the requested driver does not exist in M3.
 */
public class ConductorNoEncontradoExcepcion extends RuntimeException {

    private final UUID driverId;

    public ConductorNoEncontradoExcepcion(UUID driverId) {
        super("Conductor not found: " + driverId);
        this.driverId = driverId;
    }

    public UUID getDriverId() {
        return driverId;
    }
}
