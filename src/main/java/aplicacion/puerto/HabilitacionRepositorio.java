package m3.aplicacion.puerto;
import m3.dominio.GuardadoHabilitacion;
import m3.dominio.HabilitacionDecision;
import java.util.*;
public interface HabilitacionRepositorio {
    Optional<GuardadoHabilitacion> findByKey(UUID targetId, String actor, String key);
    void save(HabilitacionDecision decision, String key, String fingerprint);
    List<HabilitacionDecision> history(UUID driverId, UUID vehicleId, int offset, int limit);
}
