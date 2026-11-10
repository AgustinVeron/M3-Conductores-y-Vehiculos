package m3.dominio;
import java.util.UUID;
public record ViajeContexto(String tripId, UUID driverId, String clientId, String status) {}
