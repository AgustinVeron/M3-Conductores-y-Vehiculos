package m3.aplicacion.ubicacion;

import m3.aplicacion.excepcion.ConductorNoEncontradoExcepcion;
import m3.aplicacion.puerto.DisponibilidadIntencionRepositorio;
import m3.aplicacion.puerto.ConductorRepositorio;
import m3.aplicacion.puerto.M4UbicacionPuerto;
import m3.aplicacion.puerto.VehiculoRepositorio;
import m3.dominio.DisponibilidadIntencion;
import m3.dominio.Conductor;
import m3.dominio.Vehiculo;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates small, bounded movements for connected drivers and publishes them to M4.
 * The in-memory position store is intentionally isolated from the driver domain.
 */
public class UbicacionSimulacionServicio {
    private static final Logger log = LoggerFactory.getLogger(UbicacionSimulacionServicio.class);
    private static final ZoneId ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final double MIN_LATITUDE = -27.60;
    private static final double MAX_LATITUDE = -27.32;
    private static final double MIN_LONGITUDE = -59.12;
    private static final double MAX_LONGITUDE = -58.78;
    private static final double METERS_PER_DEGREE = 111_000.0;
    private static final List<GeoPosicion> RESISTENCIA_STARTS = List.of(
            new GeoPosicion(-27.4511, -58.9867),
            new GeoPosicion(-27.4698, -58.9506),
            new GeoPosicion(-27.4302, -58.9815),
            new GeoPosicion(-27.4570, -58.8306));

    private final DisponibilidadIntencionRepositorio intents;
    private final ConductorRepositorio drivers;
    private final VehiculoRepositorio vehicles;
    private final M4UbicacionPuerto m4;
    private final Clock clock;
    private final double maxStepMeters;
    private final Map<UUID, LocationSnapshot> positions = new ConcurrentHashMap<>();
    private final Set<UUID> occupiedDrivers = ConcurrentHashMap.newKeySet();

    public UbicacionSimulacionServicio(DisponibilidadIntencionRepositorio intents, ConductorRepositorio drivers,
            VehiculoRepositorio vehicles, M4UbicacionPuerto m4, Clock clock, double maxStepMeters) {
        if (!Double.isFinite(maxStepMeters) || maxStepMeters <= 0 || maxStepMeters > 500) {
            throw new IllegalArgumentException("maxStepMeters must be between 0 and 500");
        }
        this.intents = intents;
        this.drivers = drivers;
        this.vehicles = vehicles;
        this.m4 = m4;
        this.clock = clock;
        this.maxStepMeters = maxStepMeters;
    }

    /** Advances and sends exactly one point for each currently connected driver. */
    public void publishConnectedLocations() {
        for (DisponibilidadIntencion intent : intents.findConnected()) {
            if (!intent.isConnected()) continue;
            try {
                LocationSnapshot next = positions.compute(intent.getDriverId(), (driverId, current) -> {
                    GeoPosicion currentPosition = current == null ? null
                            : new GeoPosicion(current.latitude(), current.longitude());
                    GeoPosicion position = currentPosition == null ? initialPosition(driverId) : nextPosition(currentPosition);
                    return new LocationSnapshot(driverId, position.latitude(), position.longitude(), now(), false);
                });
                publish(intent, next);
                markM4Synced(intent.getDriverId());
            } catch (RuntimeException ex) {
                log.warn("[LOCATION-SIM] unable to publish driver={} reason={}", intent.getDriverId(), ex.getMessage());
            }
        }
    }

    /** Repositions a driver after M6 reports a completed trip and immediately publishes it. */
    public void setCompletedTripDestination(UUID driverId, String status, GeoPosicion destination) {
        if (!"FINALIZADO".equals(status)) {
            throw new IllegalArgumentException("Only FINALIZADO trips can update simulated location");
        }
        Conductor driver = drivers.findById(driverId).orElseThrow(() -> ConductorNoEncontradoExcepcion.forId(driverId));
        occupiedDrivers.remove(driverId);
        LocationSnapshot snapshot = new LocationSnapshot(driverId, destination.latitude(), destination.longitude(), now(), false);
        positions.put(driverId, snapshot);
        DisponibilidadIntencion intent = intents.findByDriverId(driverId).orElse(null);
        log.info("[LOCATION-SIM] M6 completed trip reposition driver={} lat={} lng={}",
                driverId, destination.latitude(), destination.longitude());
        publish(driver, intent, snapshot);
        markM4Synced(driverId);
    }

    /** Pure movement step, clamped to the Resistencia demo area. */
    public GeoPosicion nextPosition(GeoPosicion current) {
        double latitudeStep = ThreadLocalRandom.current().nextDouble(-maxStepMeters, maxStepMeters)
                / METERS_PER_DEGREE;
        double longitudeScale = Math.cos(Math.toRadians(current.latitude())) * METERS_PER_DEGREE;
        double longitudeStep = ThreadLocalRandom.current().nextDouble(-maxStepMeters, maxStepMeters)
                / longitudeScale;
        double latitude = current.latitude() + latitudeStep;
        double longitude = current.longitude() + longitudeStep;
        if (insideResistencia(current)) {
            latitude = reflect(latitude, MIN_LATITUDE, MAX_LATITUDE);
            longitude = reflect(longitude, MIN_LONGITUDE, MAX_LONGITUDE);
        }
        return new GeoPosicion(latitude, longitude);
    }

    public GeoPosicion currentPosition(UUID driverId) {
        LocationSnapshot snapshot = positions.get(driverId);
        return snapshot == null ? null : new GeoPosicion(snapshot.latitude(), snapshot.longitude());
    }

    public LocationSnapshot currentLocation(UUID driverId) {
        return positions.get(driverId);
    }

    /** Publishes busy state with location without changing the driver's persisted availability intent. */
    public void setOccupied(UUID driverId, boolean occupied) {
        if (occupied) occupiedDrivers.add(driverId);
        else occupiedDrivers.remove(driverId);
        if (!occupied) return;
        DisponibilidadIntencion intent = intents.findByDriverId(driverId).orElse(null);
        if (intent == null || !intent.isConnected()) return;
        try {
            LocationSnapshot snapshot = positions.computeIfAbsent(driverId, id -> {
                GeoPosicion initial = initialPosition(id);
                return new LocationSnapshot(id, initial.latitude(), initial.longitude(), now(), false);
            });
            publish(intent, snapshot);
            markM4Synced(driverId);
        } catch (RuntimeException ex) {
            log.warn("[LOCATION-SIM] unable to publish occupied state driver={} reason={}", driverId, ex.getMessage());
        }
    }

    private GeoPosicion initialPosition(UUID driverId) {
        return RESISTENCIA_STARTS.get(Math.floorMod(driverId.hashCode(), RESISTENCIA_STARTS.size()));
    }

    private double reflect(double value, double minimum, double maximum) {
        if (value < minimum) return minimum + (minimum - value);
        if (value > maximum) return maximum - (value - maximum);
        return value;
    }

    private boolean insideResistencia(GeoPosicion position) {
        return position.latitude() >= MIN_LATITUDE && position.latitude() <= MAX_LATITUDE
                && position.longitude() >= MIN_LONGITUDE && position.longitude() <= MAX_LONGITUDE;
    }

    private void publish(DisponibilidadIntencion intent, LocationSnapshot snapshot) {
        Conductor driver = drivers.findById(intent.getDriverId())
                .orElseThrow(() -> ConductorNoEncontradoExcepcion.forId(intent.getDriverId()));
        publish(driver, intent, snapshot);
    }

    private void publish(Conductor driver, DisponibilidadIntencion intent, LocationSnapshot snapshot) {
        Vehiculo vehicle = intent == null ? null : vehicles.findById(intent.getVehicleId()).orElse(null);
        boolean enabled = driver.getQualificationStatus() != null
                && driver.getQualificationStatus().isApproved()
                && vehicle != null && "APPROVED".equals(vehicle.getQualificationStatus());
        boolean connected = intent != null && intent.isConnected();
        boolean available = connected && intent.isAvailable() && enabled && !occupiedDrivers.contains(driver.getId());
        UbicacionActualizacion update = new UbicacionActualizacion(driver.getId(), vehicle == null ? null : vehicle.getId(), enabled,
                vehicle == null ? null : vehicle.getVehicleType(), connected, available,
                snapshot.latitude(), snapshot.longitude(), snapshot.recordedAt());
        log.info("[LOCATION-SIM] driver={} lat={} lng={}", driver.getId(), snapshot.latitude(), snapshot.longitude());
        log.info("[LOCATION-SIM] sending location to M4 driver={}", driver.getId());
        m4.sendLocation(update);
        log.info("[LOCATION-SIM] M4 sync successful driver={}", driver.getId());
    }

    private void markM4Synced(UUID driverId) {
        positions.computeIfPresent(driverId, (id, current) -> new LocationSnapshot(id,
                current.latitude(), current.longitude(), current.recordedAt(), true));
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ARGENTINA);
    }

    public record LocationSnapshot(UUID driverId, double latitude, double longitude,
            OffsetDateTime recordedAt, boolean m4Synced) {}
}
