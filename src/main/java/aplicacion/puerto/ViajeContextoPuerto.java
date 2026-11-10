package m3.aplicacion.puerto;
import m3.dominio.ViajeContexto;
import java.util.UUID;
public interface ViajeContextoPuerto {
    ViajeContexto completedTrip(String tripId, UUID driverId);
}
