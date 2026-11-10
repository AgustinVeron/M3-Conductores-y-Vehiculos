package m3.aplicacion.puerto.entrada;

import m3.dominio.Conductor;
import m3.dominio.PersonalInformacion;

/** Updates the personal data of the driver associated with the current session. */
public interface ActualizacionMiPersonalInformacionUsoCaso {
    Conductor updatePersonalInfo(PersonalInformacion personalInfo, long version);
}
