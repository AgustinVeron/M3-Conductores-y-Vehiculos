package m3.dominio;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class AuditoriaRegistro {
    private final UUID id;
    private final String entityType;
    private final UUID entityId;
    private final UUID driverId;
    private final String action;
    private final String actor;
    private final String previousStatus;
    private final String newStatus;
    private final String reason;
    private final String correlationId;
    private final Instant createdAt;

    public AuditoriaRegistro(UUID id, String entityType, UUID entityId, UUID driverId,
                    String action, String actor, String previousStatus,
                    String newStatus, String reason, String correlationId, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Audit log ID must not be null");
        this.entityType = Objects.requireNonNull(entityType, "Entity type must not be null");
        this.entityId = Objects.requireNonNull(entityId, "Entity ID must not be null");
        this.driverId = driverId;
        this.action = Objects.requireNonNull(action, "Action must not be null");
        this.actor = Objects.requireNonNull(actor, "Actor must not be null");
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.correlationId = correlationId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getId() { return id; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public UUID getDriverId() { return driverId; }
    public String getAction() { return action; }
    public String getActor() { return actor; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public String getReason() { return reason; }
    public String getCorrelationId() { return correlationId; }
    public Instant getCreatedAt() { return createdAt; }
}
