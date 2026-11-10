package m3.aplicacion.puerto;

import m3.dominio.AuditoriaRegistro;
import java.time.Instant;
import java.util.UUID;

/**
 * Output port for append-only audit logging.
 */
public interface AuditoriaRegistroRepositorio {

    void save(AuditoriaRegistro auditLog);

    /**
     * Appends an audit entry inside the current transaction.
     *
     * @param entityType    e.g. "DisponibilidadIntencion"
     * @param entityId      UUID of the affected aggregate
     * @param operation     e.g. "CREATE_INTENT" or "UPDATE_INTENT"
     * @param actor         Principal identifier (driverId, system, etc.)
     * @param correlationId Request/trace correlation identifier
     * @param changes       Non-sensitive description of changes (JSON or plain
     *                      text)
     */
    default void append(
            String entityType,
            UUID entityId,
            String operation,
            String actor,
            String correlationId,
            String changes) {
        save(new AuditoriaRegistro(
                UUID.randomUUID(),
                entityType,
                entityId,
                null,
                operation,
                actor != null ? actor : "system",
                null,
                null,
                changes,
                correlationId,
                Instant.now()));
    }
}
