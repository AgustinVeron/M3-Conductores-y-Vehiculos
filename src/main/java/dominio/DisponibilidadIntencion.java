package m3.dominio;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity: the driver's intent to be connected and available.
 *
 * <p><strong>Business rules (HU-10):</strong>
 * <ol>
 *   <li>One intent per driver (enforced by unique DB constraint and factory method).</li>
 *   <li>The driver must have a qualified (APPROVED) vehicle to set {@code available=true}.</li>
 *   <li>{@code connected=false} implies {@code available=false} — enforced in constructor
 *       and in the {@link #update} method.</li>
 *   <li>M3 only registers the intent; M4 controls actual availability (GPS, occupancy, etc.).</li>
 * </ol>
 *
 * <p>This class has <strong>no Spring, JPA or HTTP dependencies</strong>.
 */
public class DisponibilidadIntencion {

    private final UUID id;
    private final UUID driverId;
    private UUID vehicleId;

    private boolean connected;
    private boolean available;

    private IntencionEstado status;
    private long desiredVersion;
    private Long appliedVersion;

    private final Instant createdAt;
    private Instant updatedAt;
    private Instant syncedAt;

    // -------------------------------------------------------------------------
    // Constructor — used by the factory and by the persistence mapper
    // -------------------------------------------------------------------------

    /**
     * Full constructor used when rehydrating from persistence.
     */
    public DisponibilidadIntencion(
            UUID id,
            UUID driverId,
            UUID vehicleId,
            boolean connected,
            boolean available,
            IntencionEstado status,
            long desiredVersion,
            Long appliedVersion,
            Instant createdAt,
            Instant updatedAt,
            Instant syncedAt) {

        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.driverId = Objects.requireNonNull(driverId, "driverId cannot be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId cannot be null");

        // Rule 3: connected=false → available must be false
        validateConnectedAvailable(connected, available);

        this.connected = connected;
        this.available = available;
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.desiredVersion = desiredVersion;
        this.appliedVersion = appliedVersion;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
        this.syncedAt = syncedAt;
    }

    // -------------------------------------------------------------------------
    // Static factory — creates a NEW intent (first time for a driver)
    // -------------------------------------------------------------------------

    /**
     * Creates a brand-new availability intent for a driver.
     *
     * @param driverId  The driver requesting availability.
     * @param vehicleId The vehicle selected for this intent.
     * @param connected Whether the driver wants to appear connected.
     * @param available Whether the driver wants to accept trip requests.
     *                  Must be {@code false} when {@code connected} is {@code false}.
     * @param now       The current time (injected for testability).
     * @return A new {@link DisponibilidadIntencion} in {@link IntencionEstado#PENDING} state.
     */
    public static DisponibilidadIntencion create(
            UUID driverId,
            UUID vehicleId,
            boolean connected,
            boolean available,
            Instant now) {

        validateConnectedAvailable(connected, available);

        return new DisponibilidadIntencion(
                UUID.randomUUID(),
                driverId,
                vehicleId,
                connected,
                available,
                IntencionEstado.PENDING,
                1L,
                null,
                now,
                now,
                null);
    }

    // -------------------------------------------------------------------------
    // Domain behaviour
    // -------------------------------------------------------------------------

    /**
     * Updates an existing intent with new values, incrementing {@code desiredVersion}.
     *
     * @param vehicleId The vehicle to use (may differ from previous one).
     * @param connected New connected flag.
     * @param available New available flag. Must be {@code false} when connected is {@code false}.
     * @param now       Current time.
     */
    public void update(UUID vehicleId, boolean connected, boolean available, Instant now) {
        validateConnectedAvailable(connected, available);
        this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        this.connected = connected;
        this.available = available;
        this.status = IntencionEstado.PENDING;
        this.desiredVersion = this.desiredVersion + 1;
        this.updatedAt = Objects.requireNonNull(now, "now cannot be null");
    }

    /**
     * Marks this intent as successfully applied by M4 for a given version.
     *
     * @param appliedVersion The version acknowledged by M4.
     * @param now            Current time.
     */
    public void markApplied(long appliedVersion, Instant now) {
        this.status = IntencionEstado.APPLIED;
        this.appliedVersion = appliedVersion;
        this.syncedAt = now;
        this.updatedAt = now;
    }

    /**
     * Marks this intent as retrying after a failed M4 call.
     *
     * @param now Current time.
     */
    public void markRetrying(Instant now) {
        this.status = IntencionEstado.RETRYING;
        this.updatedAt = now;
    }

    /**
     * Marks this intent as permanently failed after exhausting retries.
     *
     * @param now Current time.
     */
    public void markFailed(Instant now) {
        this.status = IntencionEstado.FAILED;
        this.updatedAt = now;
    }

    // -------------------------------------------------------------------------
    // Validation helper
    // -------------------------------------------------------------------------

    private static void validateConnectedAvailable(boolean connected, boolean available) {
        if (!connected && available) {
            throw new IllegalArgumentException(
                    "available cannot be true when connected is false");
        }
    }

    // -------------------------------------------------------------------------
    // Accessors (no setters — use domain methods above)
    // -------------------------------------------------------------------------

    public UUID getId() { return id; }
    public UUID getDriverId() { return driverId; }
    public UUID getVehicleId() { return vehicleId; }
    public boolean isConnected() { return connected; }
    public boolean isAvailable() { return available; }
    public IntencionEstado getStatus() { return status; }
    public long getDesiredVersion() { return desiredVersion; }
    public Long getAppliedVersion() { return appliedVersion; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getSyncedAt() { return syncedAt; }

    // -------------------------------------------------------------------------
    // equals / hashCode by identity
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisponibilidadIntencion that = (DisponibilidadIntencion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DisponibilidadIntencion{id=" + id +
               ", driverId=" + driverId +
               ", connected=" + connected +
               ", available=" + available +
               ", status=" + status +
               ", desiredVersion=" + desiredVersion + '}';
    }
}
