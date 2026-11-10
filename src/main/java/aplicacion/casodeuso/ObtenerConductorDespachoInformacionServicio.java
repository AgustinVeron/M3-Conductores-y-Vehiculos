package m3.aplicacion.casodeuso;

import m3.aplicacion.excepcion.ConductorNoEncontradoExcepcion;
import m3.aplicacion.puerto.DisponibilidadIntencionRepositorio;
import m3.aplicacion.puerto.ConductorRepositorio;
import m3.aplicacion.puerto.VehiculoRepositorio;
import m3.aplicacion.puerto.entrada.ObtenerConductorDespachoInformacionUsoCaso;
import m3.dominio.DisponibilidadIntencion;
import m3.dominio.Conductor;
import java.util.Objects;
import java.util.UUID;

/**
 * Application query for the M5 dispatch contract.
 *
 * <p>The returned availability is exclusively the driver's persisted M3
 * intention: connected and willing to receive requests. M4 is intentionally
 * not part of this use case because occupancy, reservations and location
 * freshness are M4 concerns.</p>
 */
public class ObtenerConductorDespachoInformacionServicio implements ObtenerConductorDespachoInformacionUsoCaso {

    private final ConductorRepositorio drivers;
    private final DisponibilidadIntencionRepositorio availabilityIntents;
    private final VehiculoRepositorio vehicles;

    public ObtenerConductorDespachoInformacionServicio(
            ConductorRepositorio drivers,
            DisponibilidadIntencionRepositorio availabilityIntents,
            VehiculoRepositorio vehicles) {
        this.drivers = Objects.requireNonNull(drivers, "drivers is required");
        this.availabilityIntents = Objects.requireNonNull(availabilityIntents, "availabilityIntents is required");
        this.vehicles = Objects.requireNonNull(vehicles, "vehicles is required");
    }

    @Override
    public ConductorDespachoInformacion execute(UUID conductorId) {
        Objects.requireNonNull(conductorId, "conductorId is required");

        Conductor driver = drivers.findById(conductorId)
                .orElseThrow(() -> ConductorNoEncontradoExcepcion.forId(conductorId));

        DisponibilidadIntencion intent = availabilityIntents.findByDriverId(conductorId).orElse(null);
        if (intent == null) {
            return withoutSelectedVehicle(driver);
        }

        return vehicles.findByIdAndDriverId(intent.getVehicleId(), conductorId)
                .map(vehicle -> new ConductorDespachoInformacion(
                        conductorId,
                        driver.isOperable(),
                        intent.isConnected() && intent.isAvailable(),
                        new ConductorDespachoInformacion.VehicleInfo(vehicle.getId(), vehicle.getVehicleType()),
                        driver.getVersion()))
                // A stale intent must not make M5 see a driver as available
                // without a current M3-selected vehicle.
                .orElseGet(() -> withoutSelectedVehicle(driver));
    }

    private static ConductorDespachoInformacion withoutSelectedVehicle(Conductor driver) {
        return new ConductorDespachoInformacion(
                driver.getId(),
                driver.isOperable(),
                false,
                null,
                driver.getVersion());
    }
}
