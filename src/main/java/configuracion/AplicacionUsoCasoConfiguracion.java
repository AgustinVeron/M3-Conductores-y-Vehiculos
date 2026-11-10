package m3.configuracion;

import m3.aplicacion.puerto.AuditoriaRegistroRepositorio;
import m3.aplicacion.puerto.DisponibilidadIntencionRepositorio;
import m3.aplicacion.puerto.DocumentoRepositorio;
import m3.aplicacion.puerto.ConductorRepositorio;
import m3.aplicacion.puerto.IdempotenciaRepositorio;
import m3.aplicacion.puerto.BandejaSalidaRepositorio;
import m3.aplicacion.puerto.UnidadDeTrabajo;
import m3.aplicacion.puerto.VehiculoRepositorio;
import m3.aplicacion.casodeuso.ObtenerConductorVehiculosServicio;
import m3.aplicacion.casodeuso.ObtenerConductorVehiculosUsoCaso;
import m3.aplicacion.casodeuso.ObtenerConductorDespachoInformacionServicio;
import m3.aplicacion.casodeuso.RegistrarConductorLicenciaServicio;
import m3.aplicacion.casodeuso.RegistrarConductorLicenciaUsoCaso;
import m3.aplicacion.casodeuso.RegistrarVehiculoDocumentoServicio;
import m3.aplicacion.casodeuso.RegistrarVehiculoDocumentoUsoCaso;
import m3.aplicacion.casodeuso.SolicitudDisponibilidadServicio;
import m3.aplicacion.casodeuso.SolicitudDisponibilidadUsoCaso;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration providing Application Use Case beans for M3.
 * Follows Hexagonal Architecture: use cases are wired manually as plain Java
 * beans.
 */
@Configuration
@org.springframework.scheduling.annotation.EnableScheduling
public class AplicacionUsoCasoConfiguracion {

    @Bean
    public m3.aplicacion.casodeuso.ConductorEspacioTrabajoServicio driverWorkspaceService(
            ConductorRepositorio drivers, VehiculoRepositorio vehicles, DocumentoRepositorio documents,
            DisponibilidadIntencionRepositorio availability,
            m3.aplicacion.puerto.salida.ActualPrincipalPuerto principal, UnidadDeTrabajo transactions, Clock clock) {
        return new m3.aplicacion.casodeuso.ConductorEspacioTrabajoServicio(drivers, vehicles, documents, availability, principal, transactions, clock);
    }

    @Bean
    public m3.aplicacion.casodeuso.ConductorAccesoServicio driverAccessService(ConductorRepositorio drivers,
            m3.aplicacion.puerto.salida.ActualPrincipalPuerto principal) {
        return new m3.aplicacion.casodeuso.ConductorAccesoServicio(drivers,principal);
    }
    @Bean
    public m3.aplicacion.casodeuso.HabilitacionServicio qualificationService(ConductorRepositorio drivers,
            VehiculoRepositorio vehicles, DocumentoRepositorio documents, m3.aplicacion.puerto.HabilitacionRepositorio history,
            AuditoriaRegistroRepositorio audit, UnidadDeTrabajo transactions, m3.aplicacion.casodeuso.ConductorAccesoServicio access, Clock clock) {
        return new m3.aplicacion.casodeuso.HabilitacionServicio(drivers,vehicles,documents,history,audit,transactions,access,clock);
    }
    @Bean
    public m3.aplicacion.casodeuso.ClienteCalificacionServicio customerRatingService(
            m3.aplicacion.casodeuso.ConductorAccesoServicio access, ConductorRepositorio drivers,
            m3.aplicacion.puerto.ClienteCalificacionRepositorio ratings, m3.aplicacion.puerto.ViajeContextoPuerto trips,
            UnidadDeTrabajo transactions,Clock clock) {
        return new m3.aplicacion.casodeuso.ClienteCalificacionServicio(access,drivers,ratings,trips,transactions,clock);
    }
    @Bean
    public m3.aplicacion.casodeuso.EntregarDisponibilidadServicio deliverAvailabilityService(
            m3.aplicacion.puerto.DisponibilidadEntregaPuerto queue, m3.aplicacion.puerto.M4DisponibilidadPuerto m4,Clock clock) {
        return new m3.aplicacion.casodeuso.EntregarDisponibilidadServicio(queue,m4,clock);
    }
    // ─── Shared infrastructure ───────────────────────────────────────────────

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    // ─── HU-01 / HU-02: Conductor License ───────────────────────────────────────

    @Bean
    public RegistrarConductorLicenciaUsoCaso registerDriverLicenseUseCase(
            ConductorRepositorio driverRepository, DocumentoRepositorio documentRepository,
            UnidadDeTrabajo unitOfWork, IdempotenciaRepositorio idempotencyRepository) {
        return new RegistrarConductorLicenciaServicio(driverRepository, documentRepository,
                unitOfWork, idempotencyRepository);
    }

    // ─── HU-06: Register Vehiculo Documento ────────────────────────────────────

    @Bean
    public RegistrarVehiculoDocumentoUsoCaso registerVehicleDocumentUseCase(
            DocumentoRepositorio documentRepository,
            VehiculoRepositorio vehicleRepository,
            UnidadDeTrabajo unitOfWork, ConductorRepositorio drivers) {
        return new RegistrarVehiculoDocumentoServicio(
                documentRepository,
                vehicleRepository,
                unitOfWork, drivers);
    }

    // ─── HU-03: Vehiculo Query / Vehicles ─────────────────────────────────────

    @Bean
    public ObtenerConductorVehiculosUsoCaso getDriverVehiclesUseCase(VehiculoRepositorio vehicleRepository) {
        return new ObtenerConductorVehiculosServicio(vehicleRepository);
    }

    @Bean
    public m3.aplicacion.puerto.entrada.ObtenerConductorDespachoInformacionUsoCaso getDriverDispatchInfoUseCase(
            ConductorRepositorio driverRepository,
            DisponibilidadIntencionRepositorio availabilityIntentRepository,
            VehiculoRepositorio vehicleRepository) {
        return new ObtenerConductorDespachoInformacionServicio(
                driverRepository,
                availabilityIntentRepository,
                vehicleRepository);
    }

    // ─── HU-10: Request Availability ─────────────────────────────────────────

    @Bean
    public SolicitudDisponibilidadUsoCaso requestAvailabilityUseCase(
            ConductorRepositorio driverRepository,
            DisponibilidadIntencionRepositorio availabilityIntentRepository,
            AuditoriaRegistroRepositorio auditLogRepository,
            BandejaSalidaRepositorio outboxRepository,
            IdempotenciaRepositorio idempotencyRepository,
            UnidadDeTrabajo unitOfWork,
            Clock systemClock, DocumentoRepositorio documents, VehiculoRepositorio vehicles) {
        return new SolicitudDisponibilidadServicio(
                driverRepository,
                availabilityIntentRepository,
                auditLogRepository,
                outboxRepository,
                idempotencyRepository,
                unitOfWork,
                systemClock,documents,vehicles);
    }
}
