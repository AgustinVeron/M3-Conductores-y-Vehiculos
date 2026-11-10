package m3.dominio.excepcion;

public class PrecondicionRequeridaExcepcion extends RuntimeException {
    public PrecondicionRequeridaExcepcion(String message) {
        super(message);
    }
}
