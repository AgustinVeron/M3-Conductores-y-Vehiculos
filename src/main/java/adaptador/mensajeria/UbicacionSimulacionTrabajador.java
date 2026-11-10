package m3.adaptador.mensajeria;

import m3.aplicacion.ubicacion.UbicacionSimulacionServicio;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "location-simulator.enabled", havingValue = "true", matchIfMissing = true)
public class UbicacionSimulacionTrabajador {
    private final UbicacionSimulacionServicio simulation;

    public UbicacionSimulacionTrabajador(UbicacionSimulacionServicio simulation) {
        this.simulation = simulation;
    }

    @Scheduled(fixedDelayString = "${location-simulator.interval-ms:5000}",
            initialDelayString = "${location-simulator.interval-ms:5000}")
    public void publish() {
        simulation.publishConnectedLocations();
    }
}
