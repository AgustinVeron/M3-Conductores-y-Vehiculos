package m3.configuracion;

import m3.aplicacion.ubicacion.UbicacionSimulacionServicio;
import m3.aplicacion.puerto.DisponibilidadIntencionRepositorio;
import m3.aplicacion.puerto.ConductorRepositorio;
import m3.aplicacion.puerto.M4UbicacionPuerto;
import m3.aplicacion.puerto.M5OfertaPuerto;
import m3.aplicacion.puerto.M6ViajeCicloVidaPuerto;
import m3.aplicacion.puerto.VehiculoRepositorio;
import m3.aplicacion.viaje.ConductorViajeFlujoServicio;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UbicacionSimulacionConfiguracion {
    @Bean
    public UbicacionSimulacionServicio locationSimulationService(DisponibilidadIntencionRepositorio intents,
            ConductorRepositorio drivers, VehiculoRepositorio vehicles, M4UbicacionPuerto m4, Clock clock,
            @Value("${location-simulator.max-step-meters:35}") double maxStepMeters) {
        return new UbicacionSimulacionServicio(intents, drivers, vehicles, m4, clock, maxStepMeters);
    }

    @Bean
    public ConductorViajeFlujoServicio driverTripWorkflowService(ConductorRepositorio drivers,
            DisponibilidadIntencionRepositorio availability, M5OfertaPuerto m5, M6ViajeCicloVidaPuerto m6,
            UbicacionSimulacionServicio locations, Clock clock) {
        return new ConductorViajeFlujoServicio(drivers, availability, m5, m6, locations, clock);
    }
}
