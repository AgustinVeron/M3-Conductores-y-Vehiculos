package m3.adaptador.api;
import m3.aplicacion.puerto.DisponibilidadEntregaPuerto;
import m3.aplicacion.casodeuso.ConductorAccesoServicio;
import java.time.Clock;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
public class DisponibilidadReintentoControlador {
    private final DisponibilidadEntregaPuerto queue; private final ConductorAccesoServicio access; private final Clock clock;
    public DisponibilidadReintentoControlador(DisponibilidadEntregaPuerto queue,ConductorAccesoServicio access,Clock clock) { this.queue=queue;this.access=access;this.clock=clock; }
    @PostMapping("/api/v1/drivers/{driverId}/availability/retry")
    public ResponseEntity<Void> retry(@PathVariable UUID driverId) {
        access.driver(driverId);queue.retry(driverId,clock.instant());return ResponseEntity.accepted().build();
    }
}
