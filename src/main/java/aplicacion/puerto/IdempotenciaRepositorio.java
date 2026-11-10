package m3.aplicacion.puerto;

import m3.dominio.IdempotenciaRegistro;
import java.util.Optional;

/**
 * Output port for idempotency record management.
 */
public interface IdempotenciaRepositorio {

    Optional<IdempotenciaRegistro> findByKeyAndMethodAndPath(String key, String method, String path);

    void save(IdempotenciaRegistro record);

    /**
     * Looks up an existing record by its composite key (HU-10 compatibility).
     */
    default Optional<IdempotenciaRegistro> findByKey(
            String principal,
            String httpMethod,
            String path,
            String idempotencyKey) {
        return findByKeyAndMethodAndPath(idempotencyKey, httpMethod, path);
    }
}
