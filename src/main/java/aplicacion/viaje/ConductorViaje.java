package m3.aplicacion.viaje;

import m3.aplicacion.ubicacion.GeoPosicion;
import java.time.OffsetDateTime;

public record ConductorViaje(String tripId, ConductorOferta offer, TripStatus status,
        OffsetDateTime startedAt, OffsetDateTime completedAt, GeoPosicion destination, Integer driverRating) {
    public ConductorViaje withStatus(TripStatus newStatus, OffsetDateTime startedAt) {
        return new ConductorViaje(tripId, offer, newStatus, startedAt, completedAt, destination, driverRating);
    }

    public ConductorViaje completed(OffsetDateTime completedAt, GeoPosicion destination, Integer driverRating) {
        return new ConductorViaje(tripId, offer, TripStatus.FINALIZADO, startedAt, completedAt, destination, driverRating);
    }

    public enum TripStatus { ASSIGNED, IN_PROGRESS, FINALIZADO }
}
