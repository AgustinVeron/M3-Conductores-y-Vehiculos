package m3.adaptador.persistencia;
import m3.dominio.DisponibilidadMensaje;
import m3.aplicacion.puerto.DisponibilidadEntregaPuerto;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public class DisponibilidadEntregaPersistenciaAdaptador implements DisponibilidadEntregaPuerto {
    private final JdbcTemplate jdbc;
    public DisponibilidadEntregaPersistenciaAdaptador(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @Transactional
    public Optional<DisponibilidadMensaje> claim(Instant now,Instant leaseUntil) {
        var messages=jdbc.query("select o.id,a.driver_id,o.aggregate_version,o.payload,o.attempts from outbox o join availability_intents a on a.id=o.aggregate_id where o.message_type='m3.availability-intent.v1' and o.status in ('PENDING','PROCESSING') and o.next_attempt_at<=? order by o.next_attempt_at,o.created_at limit 1 for update skip locked",
            (r,n)->new DisponibilidadMensaje(r.getObject(1,UUID.class),r.getObject(2,UUID.class),r.getLong(3),r.getString(4),r.getInt(5)+1),Timestamp.from(now));
        if(messages.isEmpty()) return Optional.empty();
        DisponibilidadMensaje m=messages.getFirst();
        jdbc.update("update outbox set status='PROCESSING',attempts=?,next_attempt_at=? where id=?",m.attempt(),Timestamp.from(leaseUntil),m.id());
        return Optional.of(m);
    }
    @Transactional
    public void finish(DisponibilidadMensaje m,boolean applied,boolean terminal,Instant retryAt,Instant now) {
        int changed=jdbc.update("update outbox set status=?,next_attempt_at=?,processed_at=? where id=? and status='PROCESSING' and attempts=?",
            applied?"SENT":terminal?"FAILED":"PENDING",Timestamp.from(retryAt),applied||terminal?Timestamp.from(now):null,m.id(),m.attempt());
        if(changed==0) return; // A newer lease owns this delivery.
        // A late acknowledgement must never overwrite a newer requested intent.
        if(applied) jdbc.update("update availability_intents set status='APPLIED',applied_version=?,synced_at=?,updated_at=? where driver_id=? and desired_version=?",
            m.version(),Timestamp.from(now),Timestamp.from(now),m.driverId(),m.version());
        else jdbc.update("update availability_intents set status=?,updated_at=? where driver_id=? and desired_version=?",
            terminal?"FAILED":"RETRYING",Timestamp.from(now),m.driverId(),m.version());
    }
    @Transactional
    public void retry(UUID driverId,Instant now) {
        jdbc.update("update outbox set status='PENDING',attempts=0,next_attempt_at=?,processed_at=null where status='FAILED' and aggregate_id in (select id from availability_intents where driver_id=?) and aggregate_version=(select desired_version from availability_intents where driver_id=?)",
            Timestamp.from(now),driverId,driverId);
        jdbc.update("update availability_intents set status='PENDING',updated_at=? where driver_id=? and status='FAILED'",Timestamp.from(now),driverId);
    }
}
