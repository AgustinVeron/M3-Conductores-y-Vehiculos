package m3.dominio;
import java.time.Instant;
import java.util.UUID;
public record ClienteCalificacion(UUID id, UUID driverId, String tripId, String clientId, int score, String comment, Instant createdAt) {}
