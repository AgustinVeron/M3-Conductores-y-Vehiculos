package m3.aplicacion.casodeuso;

import m3.dominio.IntencionEstado;
import java.time.Instant;
import java.util.UUID;

/**
 * Result returned by the "Request Availability" use case (HU-10).
 *
 * <p>The REST controller uses this to build the 202 Accepted response.
 *
 * @param intentId       UUID of the created/updated intent.
 * @param driverId       Conductor that owns the intent.
 * @param vehicleId      Selected vehicle.
 * @param connected      Requested connected state.
 * @param available      Requested available state.
 * @param status         Synchronisation status with M4 (always PENDING after this use case).
 * @param desiredVersion Version sent to M4 for de-duplication.
 * @param updatedAt      Timestamp of the last modification.
 * @param fromCache      True when the response was replayed from an idempotency record.
 */
public record SolicitudDisponibilidadResultado(
        UUID intentId,
        UUID driverId,
        UUID vehicleId,
        boolean connected,
        boolean available,
        IntencionEstado status,
        long desiredVersion,
        Instant updatedAt,
        boolean fromCache
) {}
