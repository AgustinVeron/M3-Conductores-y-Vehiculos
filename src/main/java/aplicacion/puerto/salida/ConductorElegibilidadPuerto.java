package m3.aplicacion.puerto.salida;

import m3.dominio.elegibilidad.ConductorInstantanea;
import java.util.Optional;
import java.util.UUID;

public interface ConductorElegibilidadPuerto {
    Optional<ConductorInstantanea> findSnapshotById(UUID driverId);
    Optional<ConductorInstantanea> findSnapshotByIdentitySubject(String identitySubject);
}
