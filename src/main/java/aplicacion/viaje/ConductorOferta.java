package m3.aplicacion.viaje;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConductorOferta(UUID offerId, UUID driverId, String requestId, String pickupAddress,
        String destinationAddress, String vehicleType, OffsetDateTime expiresAt, OfferStatus status, String tripId) {
    public ConductorOferta withDecision(OfferStatus decision, String assignedTripId) {
        return new ConductorOferta(offerId, driverId, requestId, pickupAddress, destinationAddress,
                vehicleType, expiresAt, decision, assignedTripId);
    }

    public enum OfferStatus { PENDING, ACCEPTED, REJECTED, EXPIRED }
}
