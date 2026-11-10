package m3.aplicacion.puerto.entrada;

import m3.dominio.Conductor;
import java.util.UUID;

/**
 * Puerto de entrada (Caso de uso): Consultar el perfil de un conductor por su identificador único.
 * Permite la consulta si el usuario autenticado es el propietario del perfil o posee rol de Operador.
 */
public interface ObtenerConductorPerfilUsoCaso {

    /**
     * Obtiene el perfil de un conductor por su ID con validación de permisos.
     *
     * @param driverId Identificador único del conductor en M3.
     * @return El conductor encontrado con su estado actual y datos personales.
     */
    Conductor getDriverProfileById(UUID driverId);
}
