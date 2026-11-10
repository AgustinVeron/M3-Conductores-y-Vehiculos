package m3.adaptador.persistencia;
import m3.dominio.GuardadoHabilitacion;
import m3.aplicacion.puerto.HabilitacionRepositorio;
import m3.dominio.HabilitacionDecision;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class HabilitacionPersistenciaAdaptador implements HabilitacionRepositorio {
    private final JdbcTemplate jdbc; private final ObjectMapper json;
    public HabilitacionPersistenciaAdaptador(JdbcTemplate jdbc,ObjectMapper json) { this.jdbc=jdbc; this.json=json; }
    private HabilitacionDecision read(ResultSet r) throws SQLException {
        try { return json.readValue(r.getString("body"),HabilitacionDecision.class); }
        catch(java.io.IOException ex) { throw new SQLException("Invalid qualification history",ex); }
    }
    public Optional<GuardadoHabilitacion> findByKey(UUID targetId,String actor,String key) {
        return jdbc.query("select body,fingerprint from qualification_decisions where target_id=? and actor=? and request_key=?",
            (r,n)->new GuardadoHabilitacion(read(r),r.getString("fingerprint")),targetId,actor,key).stream().findFirst();
    }
    public void save(HabilitacionDecision d,String key,String fingerprint) {
        try { jdbc.update("insert into qualification_decisions(id,driver_id,vehicle_id,target_id,actor,request_key,fingerprint,body,created_at) values (?,?,?,?,?,?,?,?,?)",
            d.id(),d.driverId(),d.vehicleId(),d.vehicleId()==null?d.driverId():d.vehicleId(),d.actorSubject(),key,fingerprint,json.writeValueAsString(d),Timestamp.from(d.occurredAt())); }
        catch(com.fasterxml.jackson.core.JsonProcessingException ex) { throw new IllegalStateException(ex); }
    }
    public List<HabilitacionDecision> history(UUID driverId,UUID vehicleId,int offset,int limit) {
        return jdbc.query("select body from qualification_decisions where driver_id=? and target_id=? order by created_at desc,id desc limit ? offset ?",
            (r,n)->read(r),driverId,vehicleId==null?driverId:vehicleId,limit,offset);
    }
}
