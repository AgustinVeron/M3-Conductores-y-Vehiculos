package m3.adaptador.persistencia;
import m3.dominio.GuardadoClienteCalificacion;
import m3.aplicacion.puerto.ClienteCalificacionRepositorio;
import m3.dominio.ClienteCalificacion;
import java.util.*;
import java.sql.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class ClienteCalificacionPersistenciaAdaptador implements ClienteCalificacionRepositorio {
    private final JdbcTemplate jdbc;
    public ClienteCalificacionPersistenciaAdaptador(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    private ClienteCalificacion read(ResultSet r) throws SQLException {
        return new ClienteCalificacion(r.getObject("id",UUID.class),r.getObject("driver_id",UUID.class),r.getString("trip_id"),
            r.getString("client_id"),r.getInt("score"),r.getString("comment"),r.getTimestamp("created_at").toInstant());
    }
    public Optional<GuardadoClienteCalificacion> findByKey(UUID driver,String key) {
        return jdbc.query("select * from customer_ratings where driver_id=? and request_key=?",
            (r,n)->new GuardadoClienteCalificacion(read(r),r.getString("fingerprint")),driver,key).stream().findFirst();
    }
    public Optional<ClienteCalificacion> findByTrip(UUID driver,String trip) {
        return jdbc.query("select * from customer_ratings where driver_id=? and trip_id=?",(r,n)->read(r),driver,trip).stream().findFirst();
    }
    public void save(ClienteCalificacion r,String key,String fingerprint) {
        jdbc.update("insert into customer_ratings(id,driver_id,trip_id,client_id,score,comment,request_key,fingerprint,created_at) values(?,?,?,?,?,?,?,?,?)",
            r.id(),r.driverId(),r.tripId(),r.clientId(),r.score(),r.comment(),key,fingerprint,Timestamp.from(r.createdAt()));
    }
    public List<ClienteCalificacion> history(UUID driver,int offset,int limit) {
        return jdbc.query("select * from customer_ratings where driver_id=? order by created_at desc,id desc limit ? offset ?",(r,n)->read(r),driver,limit,offset);
    }
}
