package m3.adaptador.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request body for {@code PUT /api/v1/drivers/{driverId}/availability-intent}.
 *
 * @param vehicleId The vehicle the driver wants to use for this session.
 * @param connected Whether the driver wants to appear connected to the platform.
 * @param available Whether the driver wants to receive trip requests.
 *                  Must be {@code false} when {@code connected} is {@code false}.
 */
public record SolicitudDisponibilidadSolicitud(
        @NotNull(message = "vehicleId is required")
        UUID vehicleId,

        boolean connected,
        boolean available
) {}
