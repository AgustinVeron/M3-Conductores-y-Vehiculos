package m3.aplicacion.puerto.entrada;

import m3.dominio.elegibilidad.ElegibilidadResultado;
import java.util.UUID;

public interface VerificarConductorAptitudUsoCaso {
    ElegibilidadResultado execute(UUID driverId);
    ElegibilidadResultado executeForSelf();
    ElegibilidadResultado executeBySubject(String identitySubject);
}
