package m3.aplicacion.puerto;

import java.util.UUID;
import m3.aplicacion.viaje.OfertaDecisionComprobante;

/** Outbound M5 port for the driver's response to a dispatch offer. */
public interface M5OfertaPuerto {
    OfertaDecisionComprobante respondToOffer(UUID offerId, UUID driverId, String decision);
}
