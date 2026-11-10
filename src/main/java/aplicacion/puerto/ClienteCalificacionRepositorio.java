package m3.aplicacion.puerto;
import m3.dominio.GuardadoClienteCalificacion;
import m3.dominio.ClienteCalificacion;
import java.util.*;
public interface ClienteCalificacionRepositorio {
    Optional<GuardadoClienteCalificacion> findByKey(UUID driverId,String key);
    Optional<ClienteCalificacion> findByTrip(UUID driverId,String tripId);
    void save(ClienteCalificacion rating,String key,String fingerprint);
    List<ClienteCalificacion> history(UUID driverId,int offset,int limit);
}
