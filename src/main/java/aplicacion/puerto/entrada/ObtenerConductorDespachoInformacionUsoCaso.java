package m3.aplicacion.puerto.entrada;

import m3.aplicacion.casodeuso.ConductorDespachoInformacion;
import java.util.UUID;

/**
 * Reads the M3-owned information that M5 needs before dispatching a driver.
 */
public interface ObtenerConductorDespachoInformacionUsoCaso {

    ConductorDespachoInformacion execute(UUID conductorId);
}
