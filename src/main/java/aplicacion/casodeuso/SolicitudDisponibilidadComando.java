package m3.aplicacion.casodeuso;

import java.util.UUID;

/**
 * Command object for the "Request Availability" use case (HU-10).
 *
 * <p>Carries the driver's intent without HTTP or Spring dependencies.
 *
 * @param driverId       The driver requesting availability.
 * @param vehicleId      The vehicle selected for this session.
 * @param connected      Whether the driver wants to appear connected.
 * @param available      Whether the driver wants to accept trip requests.
 * @param idempotencyKey Client-supplied key to deduplicate retries.
 * @param requestHash    SHA-256 of the canonical request body (hex string).
 * @param principal      Authenticated principal identifier.
 * @param correlationId  Request trace identifier.
 */
public record SolicitudDisponibilidadComando(
        UUID driverId,
        UUID vehicleId,
        boolean connected,
        boolean available,
        String idempotencyKey,
        String requestHash,
        String principal,
        String correlationId
) {}
