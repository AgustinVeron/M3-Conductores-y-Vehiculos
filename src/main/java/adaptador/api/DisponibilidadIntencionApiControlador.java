package m3.adaptador.api;

import m3.adaptador.persistencia.DisponibilidadIntencionPersistenciaAdaptador;
import m3.adaptador.api.dto.DisponibilidadIntencionRespuesta;
import m3.adaptador.api.dto.SolicitudDisponibilidadSolicitud;
import m3.aplicacion.puerto.DisponibilidadIntencionRepositorio;
import m3.aplicacion.casodeuso.SolicitudDisponibilidadComando;
import m3.aplicacion.casodeuso.SolicitudDisponibilidadResultado;
import m3.aplicacion.casodeuso.SolicitudDisponibilidadUsoCaso;
import m3.dominio.excepcion.ConductorNoEncontradoExcepcion;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for availability-related endpoints (HU-10).
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code PUT  /api/v1/drivers/{driverId}/availability-intent} — driver declares intent.</li>
 *   <li>{@code GET  /api/v1/drivers/{driverId}/availability}        — read current state.</li>
 * </ul>
 *
 * <p>This controller:
 * <ul>
 *   <li>Reads and validates the {@code Idempotency-Key} header.</li>
 *   <li>Computes a SHA-256 hash of the canonical request body for idempotency deduplication.</li>
 *   <li>Resolves the authenticated principal from the request (simplified; full JWT in E1-03).</li>
 *   <li>Delegates entirely to {@link SolicitudDisponibilidadUsoCaso}.</li>
 *   <li>Never exposes JPA entities.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/drivers/{driverId}")
public class DisponibilidadIntencionApiControlador {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private static final String CORRELATION_HEADER = "X-Correlation-ID";

    private final SolicitudDisponibilidadUsoCaso requestAvailabilityUseCase;
    private final DisponibilidadIntencionRepositorio intentRepository;

    public DisponibilidadIntencionApiControlador(
            SolicitudDisponibilidadUsoCaso requestAvailabilityUseCase,
            DisponibilidadIntencionRepositorio intentRepository) {
        this.requestAvailabilityUseCase = requestAvailabilityUseCase;
        this.intentRepository = intentRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/drivers/{driverId}/availability-intent
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers or updates a driver's availability intent.
     *
     * <p>Returns {@code 202 Accepted}: the intent is persisted and enqueued to M4,
     * but M4 confirmation happens asynchronously via a worker.
     *
     * @param driverId       Path variable: the driver's UUID.
     * @param idempotencyKey Required header for deduplication of retries.
     * @param correlationId  Optional request trace ID.
     * @param body           Request body with vehicle, connected and available flags.
     * @return 202 with the serialized intent state.
     */
    @PutMapping("/availability-intent")
    public ResponseEntity<DisponibilidadIntencionRespuesta> requestAvailability(
            @PathVariable UUID driverId,
            @RequestHeader(IDEMPOTENCY_HEADER) String idempotencyKey,
            @RequestHeader(value = CORRELATION_HEADER, required = false) String correlationId,
            @Valid @RequestBody SolicitudDisponibilidadSolicitud body, jakarta.servlet.http.HttpServletRequest servletRequest) {

        String canonicalBody = toCanonical(driverId, body);
        String requestHash = sha256Hex(canonicalBody);
        String principal = java.util.Objects.toString(servletRequest.getAttribute("m3.actor"), "driver:"+driverId);
        String corrId = correlationId != null ? correlationId : UUID.randomUUID().toString();

        SolicitudDisponibilidadComando cmd = new SolicitudDisponibilidadComando(
                driverId,
                body.vehicleId(),
                body.connected(),
                body.available(),
                idempotencyKey,
                requestHash,
                principal,
                corrId);

        SolicitudDisponibilidadResultado result = requestAvailabilityUseCase.execute(cmd);

        DisponibilidadIntencionRespuesta response = toResponse(result);

        // 202 Accepted: intent persisted, M4 sync pending
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .header(CORRELATION_HEADER, corrId).eTag(String.valueOf(result.desiredVersion()))
                .body(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/drivers/{driverId}/availability
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the current availability state of the driver.
     *
     * <p>Returns {@code 200 OK} with the persisted intent, or {@code 404} if none found.
     *
     * @param driverId Path variable: the driver's UUID.
     * @return 200 with current availability state.
     */
    @GetMapping("/availability")
    public ResponseEntity<DisponibilidadIntencionRespuesta> getAvailability(
            @PathVariable UUID driverId) {

        return intentRepository.findByDriverId(driverId)
                .map(intent -> new DisponibilidadIntencionRespuesta(
                        intent.getId(),
                        intent.getDriverId(),
                        intent.getVehicleId(),
                        intent.isConnected(),
                        intent.isAvailable(),
                        intent.getStatus(),
                        intent.getDesiredVersion(),
                        intent.getUpdatedAt(),intent.getAppliedVersion()))
                .map(body -> ResponseEntity.ok().eTag(String.valueOf(body.desiredVersion())).body(body))
                .orElseThrow(() -> new ConductorNoEncontradoExcepcion(driverId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static DisponibilidadIntencionRespuesta toResponse(SolicitudDisponibilidadResultado r) {
        return new DisponibilidadIntencionRespuesta(
                r.intentId(),
                r.driverId(),
                r.vehicleId(),
                r.connected(),
                r.available(),
                r.status(),
                r.desiredVersion(),
                r.updatedAt());
    }

    /**
     * Builds a deterministic canonical string from the request for hash computation.
     * Order of fields must be stable; no whitespace variance.
     */
    private static String toCanonical(UUID driverId, SolicitudDisponibilidadSolicitud body) {
        return "driverId=" + driverId +
               "&vehicleId=" + body.vehicleId() +
               "&connected=" + body.connected() +
               "&available=" + body.available();
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Simplified principal resolution. Will be replaced by JWT extraction in E1-03
     * (B4 task: Spring Security Resource Server).
     */
    private static String resolvePrincipal(UUID driverId) {
        return "driver:" + driverId;
    }
}
