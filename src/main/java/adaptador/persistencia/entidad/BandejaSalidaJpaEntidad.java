package m3.adaptador.persistencia.entidad;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping the {@code outbox} table.
 * Used for the transactional HTTP outbox pattern (M4 intent delivery, E1).
 */
@Entity
@Table(name = "outbox")
public class BandejaSalidaJpaEntidad {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "destination", nullable = false, length = 200)
    private String destination;

    @Column(name = "message_type", nullable = false, length = 100)
    private String messageType;

    @Column(name = "aggregate_type", nullable = false, length = 80)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_version", nullable = false)
    private long aggregateVersion;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING | PROCESSING | SENT | FAILED

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected BandejaSalidaJpaEntidad() {}

    public BandejaSalidaJpaEntidad(
            UUID id, String destination, String messageType,
            String aggregateType, UUID aggregateId, long aggregateVersion,
            String payload, String status, int attempts,
            Instant nextAttemptAt, Instant createdAt) {
        this.id = id;
        this.destination = destination;
        this.messageType = messageType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.payload = payload;
        this.status = status;
        this.attempts = attempts;
        this.nextAttemptAt = nextAttemptAt;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getDestination() { return destination; }
    public String getMessageType() { return messageType; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public long getAggregateVersion() { return aggregateVersion; }
    public String getPayload() { return payload; }
    public String getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getProcessedAt() { return processedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public void setNextAttemptAt(Instant nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
