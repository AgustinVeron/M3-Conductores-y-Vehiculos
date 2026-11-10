package m3.aplicacion.excepcion;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra un perfil de conductor en el sistema.
 */
public class ConductorNoEncontradoExcepcion extends RuntimeException {

    public ConductorNoEncontradoExcepcion(String message) {
        super(message);
    }

    public static ConductorNoEncontradoExcepcion forId(UUID driverId) {
        return new ConductorNoEncontradoExcepcion("No se encontró ningún conductor registrado con el ID: " + driverId);
    }

    public static ConductorNoEncontradoExcepcion forSubject(String identitySubject) {
        return new ConductorNoEncontradoExcepcion("No se encontró ningún perfil de conductor vinculado a la identidad: " + identitySubject);
    }
}
