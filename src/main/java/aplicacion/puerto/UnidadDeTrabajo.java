package m3.aplicacion.puerto;

import java.util.function.Supplier;

/**
 * Output port for managing transactional boundaries (Unit of Work).
 */
public interface UnidadDeTrabajo {

    <T> T executeInTransaction(Supplier<T> action);

    void executeInTransaction(Runnable action);
}

