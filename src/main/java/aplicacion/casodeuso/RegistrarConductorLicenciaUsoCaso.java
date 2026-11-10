package m3.aplicacion.casodeuso;

import m3.dominio.Documento;
import m3.dominio.Conductor;
import java.util.List;
import java.util.UUID;

public interface RegistrarConductorLicenciaUsoCaso {
    Documento register(RegistrarConductorLicenciaComando command);
    List<Documento> history(UUID driverId);
    Conductor driver(UUID driverId);
}
