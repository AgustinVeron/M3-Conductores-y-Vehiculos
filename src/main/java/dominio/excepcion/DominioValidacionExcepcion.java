package m3.dominio.excepcion;

public class DominioValidacionExcepcion extends RuntimeException {
    public DominioValidacionExcepcion(String message) {
        super(message);
    }
}
