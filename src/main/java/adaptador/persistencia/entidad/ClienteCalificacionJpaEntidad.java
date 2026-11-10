package m3.adaptador.persistencia.entidad;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="customer_ratings",uniqueConstraints=@UniqueConstraint(columnNames={"driver_id","trip_id"}))
public class ClienteCalificacionJpaEntidad {
    @Id
    @Column(name="id")
    private UUID id;
    @Column(name="driver_id")
    private UUID driverId;
    @Column(name="trip_id", length=128)
    private String tripId;
    @Column(name="client_id", length=128)
    private String clientId;
    @Column(name="score")
    private int score;
    @Column(name="comment", length=500)
    private String comment;
    @Column(name="request_key", length=128)
    private String requestKey;
    @Column(name="fingerprint", length=700)
    private String fingerprint;
    @Column(name="created_at")
    private Instant createdAt;
}
