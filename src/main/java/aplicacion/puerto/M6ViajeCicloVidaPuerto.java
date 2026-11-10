package m3.aplicacion.puerto;

import java.util.UUID;

/** Outbound M6 port for starting and requesting completion of an assigned trip. */
public interface M6ViajeCicloVidaPuerto {
    void startTrip(String tripId, UUID driverId);
    void requestTripCompletion(String tripId, UUID driverId);
}
