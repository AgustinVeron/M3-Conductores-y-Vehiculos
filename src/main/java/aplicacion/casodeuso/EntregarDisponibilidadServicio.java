package m3.aplicacion.casodeuso;
import m3.dominio.EntregaResultado;
import m3.aplicacion.puerto.*;
import java.time.Clock;
public class EntregarDisponibilidadServicio {
    private final DisponibilidadEntregaPuerto queue; private final M4DisponibilidadPuerto m4; private final Clock clock;
    public EntregarDisponibilidadServicio(DisponibilidadEntregaPuerto queue,M4DisponibilidadPuerto m4,Clock clock) { this.queue=queue; this.m4=m4; this.clock=clock; }
    public void deliverBatch() {
        for(int i=0;i<20;i++) {
            var now=clock.instant(); var claimed=queue.claim(now,now.plusSeconds(60));
            if(claimed.isEmpty()) return;
            var message=claimed.get(); EntregaResultado result;
            try { result=m4.send(message); } catch(RuntimeException ex) { result=EntregaResultado.RETRY; }
            boolean applied=result==EntregaResultado.APPLIED;
            boolean terminal=result==EntregaResultado.REJECTED || message.attempt()>=6;
            var finished=clock.instant();
            queue.finish(message,applied,terminal,finished.plusSeconds(Math.min(60,1L << Math.min(message.attempt(),6))),finished);
        }
    }
}
