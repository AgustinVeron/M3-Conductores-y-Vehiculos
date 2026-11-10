package m3.adaptador.mensajeria;
import m3.aplicacion.casodeuso.EntregarDisponibilidadServicio;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
@ConditionalOnProperty(name="m3.integracion.worker-enabled",havingValue="true",matchIfMissing=true)
public class DisponibilidadTrabajador {
    private final EntregarDisponibilidadServicio delivery;
    public DisponibilidadTrabajador(EntregarDisponibilidadServicio delivery) { this.delivery=delivery; }
    @Scheduled(fixedDelayString="${m3.integration.poll-ms:5000}",initialDelayString="${m3.integration.poll-ms:5000}")
    public void send() { delivery.deliverBatch(); }
}
