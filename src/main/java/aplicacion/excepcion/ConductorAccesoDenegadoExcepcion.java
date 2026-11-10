package m3.aplicacion.excepcion;

/**
 * Excepción lanzada cuando un usuario intenta acceder o consultar los datos de un conductor
 * sin poseer los permisos de propietario ni el rol administrativo de Operador.
 */
public class ConductorAccesoDenegadoExcepcion extends RuntimeException {

    public ConductorAccesoDenegadoExcepcion(String message) {
        super(message);
    }
}
