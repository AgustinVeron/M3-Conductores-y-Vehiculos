package m3.aplicacion.casodeuso;

import java.util.UUID;

/**
 * Read model for the M5 dispatch integration. It deliberately contains M3's
 * availability intent, not M4's effective availability or trip state.
 */
public record ConductorDespachoInformacion(
        UUID conductorId,
        boolean habilitado,
        boolean disponible,
        VehicleInfo vehiculo,
        long driverVersion) {

    public record VehicleInfo(UUID vehiculoId, String tipoVehiculo) {
    }
}
