package m3.aplicacion.ubicacion;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Location payload sent to M4; M3 suppresses availability while a driver has an assigned trip. */
public record UbicacionActualizacion(
        UUID driverId,
        UUID vehicleId,
        boolean enabled,
        String vehicleType,
        boolean connected,
        boolean available,
        double latitude,
        double longitude,
        OffsetDateTime recordedAt) {
}
