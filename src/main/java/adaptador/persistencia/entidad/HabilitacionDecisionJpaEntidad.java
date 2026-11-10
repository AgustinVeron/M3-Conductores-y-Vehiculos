package m3.adaptador.persistencia.entidad;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="qualification_decisions",uniqueConstraints=@UniqueConstraint(columnNames={"target_id","actor","request_key"}))
public class HabilitacionDecisionJpaEntidad {
    @Id
    @Column(name="id")
    private UUID id;
    @Column(name="driver_id")
    private UUID driverId;
    @Column(name="vehicle_id")
    private UUID vehicleId;
    @Column(name="target_id")
    private UUID targetId;
    @Column(name="actor", length=128)
    private String actor;
    @Column(name="request_key", length=128)
    private String requestKey;
    @Column(name="fingerprint", length=600)
    private String fingerprint;
    @Column(name="body", columnDefinition="TEXT")
    private String body;
    @Column(name="created_at")
    private Instant createdAt;
}
