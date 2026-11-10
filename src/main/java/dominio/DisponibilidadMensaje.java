package m3.dominio;
import java.util.UUID;
public record DisponibilidadMensaje(UUID id, UUID driverId,long version,String payload,int attempt) {}
