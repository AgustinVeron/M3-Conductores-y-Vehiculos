package m3.dominio;
import java.time.Instant;
import java.util.UUID;
public record HabilitacionDecision(UUID id, UUID driverId, UUID vehicleId, HabilitacionEstado previousStatus,
        HabilitacionEstado newStatus, String reason, String actorSubject, long driverVersion, Instant occurredAt) {}
