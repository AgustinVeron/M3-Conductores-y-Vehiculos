package m3.adaptador.api;

import m3.adaptador.api.dto.ViajeCompletadaUbicacionSolicitud;
import m3.aplicacion.ubicacion.GeoPosicion;
import m3.aplicacion.viaje.ConductorViajeFlujoServicio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Receives M6's trip-completed notification and moves the simulator to its destination. */
@RestController
@RequestMapping("/api/v1/integration/m6/trips")
public class M6ViajeCompletadaUbicacionControlador {
    private final ConductorViajeFlujoServicio workflow;

    public M6ViajeCompletadaUbicacionControlador(ConductorViajeFlujoServicio workflow) {
        this.workflow = workflow;
    }

    @PostMapping("/completed")
    public ResponseEntity<Void> completed(@Valid @RequestBody ViajeCompletadaUbicacionSolicitud request) {
        if (!"FINALIZADO".equals(request.status())) {
            throw new IllegalArgumentException("M6 debe informar el estado FINALIZADO.");
        }
        workflow.completedByM6(request.tripId(), request.driverId(),
                new GeoPosicion(request.destination().latitude(), request.destination().longitude()),
                request.driverRating(), request.completedAt());
        return ResponseEntity.accepted().build();
    }
}
