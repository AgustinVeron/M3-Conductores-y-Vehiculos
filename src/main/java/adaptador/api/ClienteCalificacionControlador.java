package m3.adaptador.api;
import m3.aplicacion.casodeuso.ClienteCalificacionServicio;
import m3.dominio.ClienteCalificacion;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/v1/drivers/{driverId}/customer-ratings")
public class ClienteCalificacionControlador {
    private final ClienteCalificacionServicio service;
    public ClienteCalificacionControlador(ClienteCalificacionServicio service) { this.service=service; }
    public record Rating(String tripId,int score,String comment) {}
    @PostMapping public ClienteCalificacion rate(@PathVariable UUID driverId,@RequestBody Rating body,@RequestHeader("Idempotency-Key") String key) {
        return service.rate(driverId,body.tripId(),body.score(),body.comment(),key);
    }
    @GetMapping public List<ClienteCalificacion> history(@PathVariable UUID driverId,@RequestParam(defaultValue="0") int offset,@RequestParam(defaultValue="25") int limit) {
        return service.history(driverId,offset,limit);
    }
}
