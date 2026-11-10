package m3.aplicacion.excepcion;

/**
 * Excepción lanzada cuando ya existe un conductor con el identity_subject indicado.
 */
public class ConductorYaExisteExcepcion extends RuntimeException {

    private final String identitySubject;

    public ConductorYaExisteExcepcion(String identitySubject) {
        super("Ya existe un perfil de conductor registrado para la identidad: " + identitySubject);
        this.identitySubject = identitySubject;
    }

    public String getIdentitySubject() {
        return identitySubject;
    }
}
