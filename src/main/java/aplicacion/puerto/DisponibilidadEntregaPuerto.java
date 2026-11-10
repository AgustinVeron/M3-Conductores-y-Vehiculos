package m3.aplicacion.puerto;
import m3.dominio.DisponibilidadMensaje;
import java.time.Instant;
import java.util.*;
public interface DisponibilidadEntregaPuerto {
    Optional<DisponibilidadMensaje> claim(Instant now,Instant leaseUntil);
    void finish(DisponibilidadMensaje message,boolean applied,boolean terminal,Instant retryAt,Instant now);
    void retry(UUID driverId,Instant now);
}
