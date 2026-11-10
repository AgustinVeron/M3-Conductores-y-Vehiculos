package m3.adaptador.persistencia;

import m3.adaptador.persistencia.entidad.DisponibilidadIntencionJpaEntidad;
import m3.adaptador.persistencia.entidad.BandejaSalidaJpaEntidad;
import m3.adaptador.persistencia.repositorio.SpringDatosDisponibilidadIntencionRepositorio;
import m3.adaptador.persistencia.repositorio.SpringDatosBandejaSalidaRepositorio;
import m3.aplicacion.puerto.DisponibilidadIntencionRepositorio;
import m3.aplicacion.puerto.BandejaSalidaRepositorio;
import m3.dominio.DisponibilidadIntencion;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Persistence adapter implementing {@link DisponibilidadIntencionRepositorio} and {@link BandejaSalidaRepositorio}.
 *
 * <p>All writes happen inside the caller's transaction (UnidadDeTrabajo).
 */
@Component
public class DisponibilidadIntencionPersistenciaAdaptador
    implements DisponibilidadIntencionRepositorio, BandejaSalidaRepositorio {

    private static final String M4_AVAILABILITY_INTENT_MESSAGE_TYPE = "m3.availability-intent.v1";

    private final SpringDatosDisponibilidadIntencionRepositorio intentRepo;
    private final SpringDatosBandejaSalidaRepositorio outboxRepo;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final String m4BaseUrl;

    public DisponibilidadIntencionPersistenciaAdaptador(
            SpringDatosDisponibilidadIntencionRepositorio intentRepo,
            SpringDatosBandejaSalidaRepositorio outboxRepo,
            ObjectMapper objectMapper,
            Clock clock,
            @Value("${m3.integration.m4-base-url:http://localhost:4004}") String m4BaseUrl) {
        this.intentRepo = Objects.requireNonNull(intentRepo);
        this.outboxRepo = Objects.requireNonNull(outboxRepo);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.clock = Objects.requireNonNull(clock);
        this.m4BaseUrl = Objects.requireNonNull(m4BaseUrl);
    }

    // ─── DisponibilidadIntencionRepositorio ────────────────────────────────────────

    @Override
    public Optional<DisponibilidadIntencion> findByDriverId(UUID driverId) {
        return intentRepo.findByDriverId(driverId).map(this::toDomain);
    }

    @Override
    public List<DisponibilidadIntencion> findConnected() {
        return intentRepo.findByConnectedTrue().stream().map(this::toDomain).toList();
    }

    @Override
    public DisponibilidadIntencion save(DisponibilidadIntencion intent) {
        DisponibilidadIntencionJpaEntidad entity = toJpa(intent);
        DisponibilidadIntencionJpaEntidad saved = intentRepo.save(entity);
        return toDomain(saved);
    }

    // ─── BandejaSalidaRepositorio ────────────────────────────────────────────────────

    @Override
    public void enqueue(DisponibilidadIntencion intent, String correlationId) {
        UUID commandId=UUID.randomUUID();
        String payload = buildM4Payload(intent, correlationId,commandId);
        String destination = m4BaseUrl +
            "/api/v1/drivers/" + intent.getDriverId() + "/availability-intent";

        BandejaSalidaJpaEntidad entry = new BandejaSalidaJpaEntidad(
                commandId,
                destination,
                M4_AVAILABILITY_INTENT_MESSAGE_TYPE,
                "DisponibilidadIntencion",
                intent.getId(),
                intent.getDesiredVersion(),
                payload,
                "PENDING",
                0,
                Instant.now(clock),
                Instant.now(clock));
        outboxRepo.save(entry);
    }

    // ─── Mappers ─────────────────────────────────────────────────────────────

    private DisponibilidadIntencion toDomain(DisponibilidadIntencionJpaEntidad e) {
        return new DisponibilidadIntencion(
                e.getId(), e.getDriverId(), e.getVehicleId(),
                e.isConnected(), e.isAvailable(),
                e.getStatus(), e.getDesiredVersion(), e.getAppliedVersion(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getSyncedAt());
    }

    private DisponibilidadIntencionJpaEntidad toJpa(DisponibilidadIntencion d) {
        return new DisponibilidadIntencionJpaEntidad(
                d.getId(), d.getDriverId(), d.getVehicleId(),
                d.isConnected(), d.isAvailable(),
                d.getStatus(), d.getDesiredVersion(), d.getAppliedVersion(),
                d.getCreatedAt(), d.getUpdatedAt(), d.getSyncedAt());
    }

    private String buildM4Payload(DisponibilidadIntencion intent, String correlationId,UUID commandId) {
        Map<String, Object> body = Map.of(
                "commandId", commandId.toString(),
                "driverId", intent.getDriverId().toString(),
                "vehicleId", intent.getVehicleId().toString(),
                "connected", intent.isConnected(),
                "available", intent.isAvailable(),
                "desiredVersion", intent.getDesiredVersion(),
                "correlationId", correlationId != null ? correlationId : "");
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize M4 payload", ex);
        }
    }
}
