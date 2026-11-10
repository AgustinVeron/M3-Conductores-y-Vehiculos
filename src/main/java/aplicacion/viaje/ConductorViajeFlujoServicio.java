package m3.aplicacion.viaje;

import m3.aplicacion.excepcion.ConductorNoEncontradoExcepcion;
import m3.aplicacion.ubicacion.GeoPosicion;
import m3.aplicacion.ubicacion.UbicacionSimulacionServicio;
import m3.aplicacion.puerto.DisponibilidadIntencionRepositorio;
import m3.aplicacion.puerto.ConductorRepositorio;
import m3.aplicacion.puerto.M5OfertaPuerto;
import m3.aplicacion.puerto.M6ViajeCicloVidaPuerto;
import m3.dominio.DisponibilidadIntencion;
import m3.dominio.excepcion.DominioValidacionExcepcion;
import m3.dominio.excepcion.IntegracionNoDisponibleExcepcion;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Coordinates M5/M6 requests and the small amount of trip state M3 needs for its driver UI. */
public class ConductorViajeFlujoServicio {
    private static final Logger log = LoggerFactory.getLogger(ConductorViajeFlujoServicio.class);
    private static final ZoneId ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final ConductorRepositorio drivers;
    private final DisponibilidadIntencionRepositorio availability;
    private final M5OfertaPuerto m5;
    private final M6ViajeCicloVidaPuerto m6;
    private final UbicacionSimulacionServicio locations;
    private final Clock clock;
    private final Map<UUID, ConductorOferta> offers = new ConcurrentHashMap<>();
    private final Map<UUID, ConductorViaje> trips = new ConcurrentHashMap<>();
    private final Map<String, ConductorViaje> completedEvents = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Integer>> ratings = new ConcurrentHashMap<>();

    public ConductorViajeFlujoServicio(ConductorRepositorio drivers, DisponibilidadIntencionRepositorio availability,
            M5OfertaPuerto m5, M6ViajeCicloVidaPuerto m6, UbicacionSimulacionServicio locations, Clock clock) {
        this.drivers = drivers;
        this.availability = availability;
        this.m5 = m5;
        this.m6 = m6;
        this.locations = locations;
        this.clock = clock;
    }

    public ConductorOferta receiveOffer(ConductorOferta incoming) {
        drivers.findById(incoming.driverId()).orElseThrow(() -> ConductorNoEncontradoExcepcion.forId(incoming.driverId()));
        ConductorOferta existing = offers.get(incoming.offerId());
        if (existing != null) {
            if (!sameOffer(existing, incoming)) {
                throw new DominioValidacionExcepcion("M5 reutilizó el identificador de una oferta con datos diferentes.");
            }
            return existing;
        }
        DisponibilidadIntencion intent = availability.findByDriverId(incoming.driverId())
                .orElseThrow(() -> new DominioValidacionExcepcion("El conductor no tiene una intención de disponibilidad activa."));
        if (!intent.isConnected() || !intent.isAvailable()) {
            throw new DominioValidacionExcepcion("El conductor no está conectado y disponible para recibir ofertas.");
        }
        if (incoming.expiresAt() == null || !incoming.expiresAt().isAfter(now())) {
            throw new DominioValidacionExcepcion("La oferta de M5 ya venció.");
        }
        ConductorViaje active = trips.get(incoming.driverId());
        if (active != null && active.status() != ConductorViaje.TripStatus.FINALIZADO) {
            throw new DominioValidacionExcepcion("El conductor ya tiene un viaje asignado.");
        }
        ConductorOferta raced = offers.putIfAbsent(incoming.offerId(), incoming);
        if (raced != null && !sameOffer(raced, incoming)) {
            throw new DominioValidacionExcepcion("M5 reutilizó el identificador de una oferta con datos diferentes.");
        }
        if (raced == null) log.info("[TRIP] driver={} received offer={}", incoming.driverId(), incoming.offerId());
        return raced == null ? incoming : raced;
    }

    public List<ConductorOferta> offersFor(UUID driverId) {
        return offers.values().stream().filter(o -> o.driverId().equals(driverId)
                        && o.status() == ConductorOferta.OfferStatus.PENDING && o.expiresAt().isAfter(now()))
                .sorted(Comparator.comparing(ConductorOferta::expiresAt)).toList();
    }

    public synchronized ConductorOferta respond(UUID driverId, UUID offerId, String decision) {
        ConductorOferta offer = offers.get(offerId);
        if (offer == null || !offer.driverId().equals(driverId)) {
            throw new DominioValidacionExcepcion("No se encontró esa oferta para el conductor actual.");
        }
        if (offer.status() != ConductorOferta.OfferStatus.PENDING || !offer.expiresAt().isAfter(now())) {
            throw new DominioValidacionExcepcion("La oferta ya fue respondida o venció.");
        }
        if (!"ACCEPTED".equals(decision) && !"REJECTED".equals(decision)) {
            throw new IllegalArgumentException("La respuesta debe ser ACCEPTED o REJECTED.");
        }
        if ("ACCEPTED".equals(decision)) {
            DisponibilidadIntencion intent = availability.findByDriverId(driverId).orElse(null);
            if (intent == null || !intent.isConnected() || !intent.isAvailable()) {
                throw new DominioValidacionExcepcion("Conectate y activá la disponibilidad antes de aceptar el pedido.");
            }
            ConductorViaje active = trips.get(driverId);
            if (active != null && active.status() != ConductorViaje.TripStatus.FINALIZADO) {
                throw new DominioValidacionExcepcion("El conductor ya tiene un viaje en curso.");
            }
        }
        OfertaDecisionComprobante receipt = m5.respondToOffer(offerId, driverId, decision);
        if (receipt == null || !decision.equals(receipt.status())) {
            throw new IntegracionNoDisponibleExcepcion("M5 no confirmó la respuesta a la oferta.");
        }
        if ("ACCEPTED".equals(decision)) {
            if (receipt.tripId() == null || receipt.tripId().isBlank()) {
                throw new IntegracionNoDisponibleExcepcion("M5 aceptó la oferta sin informar el viaje asignado.");
            }
            ConductorOferta accepted = offer.withDecision(ConductorOferta.OfferStatus.ACCEPTED, receipt.tripId());
            offers.put(offerId, accepted);
            trips.put(driverId, new ConductorViaje(receipt.tripId(), accepted, ConductorViaje.TripStatus.ASSIGNED,
                    null, null, null, null));
            locations.setOccupied(driverId, true);
            log.info("[TRIP] driver={} accepted offer={} trip={}", driverId, offerId, receipt.tripId());
            return accepted;
        }
        ConductorOferta rejected = offer.withDecision(ConductorOferta.OfferStatus.REJECTED, null);
        offers.put(offerId, rejected);
        log.info("[TRIP] driver={} rejected offer={}", driverId, offerId);
        return rejected;
    }

    public synchronized ConductorViaje start(UUID driverId, String tripId) {
        ConductorViaje trip = requireTrip(driverId, tripId);
        if (trip.status() != ConductorViaje.TripStatus.ASSIGNED) {
            throw new DominioValidacionExcepcion("El viaje no está esperando el inicio.");
        }
        m6.startTrip(tripId, driverId);
        ConductorViaje started = trip.withStatus(ConductorViaje.TripStatus.IN_PROGRESS, now());
        trips.put(driverId, started);
        log.info("[TRIP] driver={} trip={} started", driverId, tripId);
        return started;
    }

    public synchronized void requestCompletion(UUID driverId, String tripId) {
        ConductorViaje trip = requireTrip(driverId, tripId);
        if (trip.status() != ConductorViaje.TripStatus.IN_PROGRESS) {
            throw new DominioValidacionExcepcion("Sólo se puede finalizar un viaje que está en curso.");
        }
        m6.requestTripCompletion(tripId, driverId);
        log.info("[TRIP] driver={} trip={} completion requested from M6", driverId, tripId);
    }

    /** Applies M6's final event once, moves to the destination, and sends that point to M4. */
    public ConductorViaje completedByM6(String tripId, UUID driverId, GeoPosicion destination,
            Integer driverRating, OffsetDateTime completedAt) {
        if (driverRating != null && (driverRating < 1 || driverRating > 5)) {
            throw new IllegalArgumentException("La puntuación de M6 debe estar entre 1 y 5.");
        }
        String eventKey = driverId + ":" + tripId;
        return completedEvents.compute(eventKey, (ignored, previous) -> {
            if (previous != null) {
                if (!java.util.Objects.equals(previous.destination(), destination)
                        || !java.util.Objects.equals(previous.driverRating(), driverRating)
                        || (completedAt != null && !java.util.Objects.equals(previous.completedAt(), completedAt))) {
                    throw new DominioValidacionExcepcion("M6 reenvió la finalización con datos diferentes.");
                }
                return previous;
            }
            ConductorViaje trip = requireTrip(driverId, tripId);
            if (trip.status() != ConductorViaje.TripStatus.IN_PROGRESS) {
                throw new DominioValidacionExcepcion("M6 informó un viaje que M3 no tiene en curso.");
            }
            locations.setCompletedTripDestination(driverId, "FINALIZADO", destination);
            ConductorViaje completed = trip.completed(completedAt == null ? now() : completedAt, destination, driverRating);
            trips.put(driverId, completed);
            if (driverRating != null) {
                ratings.computeIfAbsent(driverId, key -> new ConcurrentHashMap<>()).putIfAbsent(tripId, driverRating);
            }
            log.info("[TRIP] driver={} trip={} finalized by M6", driverId, tripId);
            return completed;
        });
    }

    public ConductorViaje currentTrip(UUID driverId) {
        return trips.get(driverId);
    }

    public RatingSummary ratingSummary(UUID driverId) {
        Map<String, Integer> driverRatings = ratings.getOrDefault(driverId, Map.of());
        if (driverRatings.isEmpty()) return new RatingSummary(0, 0);
        double average = driverRatings.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        return new RatingSummary(average, driverRatings.size());
    }

    private ConductorViaje requireTrip(UUID driverId, String tripId) {
        ConductorViaje trip = trips.get(driverId);
        if (trip == null || !trip.tripId().equals(tripId)) {
            throw new DominioValidacionExcepcion("No se encontró ese viaje para el conductor actual.");
        }
        return trip;
    }

    private boolean sameOffer(ConductorOferta first, ConductorOferta second) {
        return first.offerId().equals(second.offerId()) && first.driverId().equals(second.driverId())
                && java.util.Objects.equals(first.requestId(), second.requestId())
                && java.util.Objects.equals(first.pickupAddress(), second.pickupAddress())
                && java.util.Objects.equals(first.destinationAddress(), second.destinationAddress())
                && java.util.Objects.equals(first.vehicleType(), second.vehicleType())
                && java.util.Objects.equals(first.expiresAt(), second.expiresAt());
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ARGENTINA);
    }

    public record RatingSummary(double average, int count) {}
}
