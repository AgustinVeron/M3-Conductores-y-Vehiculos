package m3.adaptador.api;

import m3.adaptador.api.dto.M5ConductorOfertaSolicitud;
import m3.aplicacion.viaje.ConductorOferta;
import m3.aplicacion.viaje.ConductorViajeFlujoServicio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Receives an offer already dispatched by M5; M3 does not perform dispatch. */
@RestController
@RequestMapping("/api/v1/integration/m5/offers")
public class M5ConductorOfertaControlador {
    private final ConductorViajeFlujoServicio workflow;

    public M5ConductorOfertaControlador(ConductorViajeFlujoServicio workflow) {
        this.workflow = workflow;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@Valid @RequestBody M5ConductorOfertaSolicitud request) {
        workflow.receiveOffer(new ConductorOferta(request.offerId(), request.driverId(), request.requestId(),
                request.pickupAddress(), request.destinationAddress(), request.vehicleType(), request.expiresAt(),
                ConductorOferta.OfferStatus.PENDING, null));
        return ResponseEntity.accepted().build();
    }
}
