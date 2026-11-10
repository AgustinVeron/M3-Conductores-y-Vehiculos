package m3.dominio.excepcion;

/**
 * Thrown when an {@code Idempotency-Key} is reused with a different request
 * body.
 * Maps to HTTP 409 Conflict.
 */
public class IdempotenciaConflictoExcepcion extends RuntimeException {

    private final String idempotencyKey;

    public IdempotenciaConflictoExcepcion(String messageOrKey) {
        super(messageOrKey != null && messageOrKey.contains(" ")
                ? messageOrKey
                : "Idempotency-Key '" + messageOrKey + "' was already used with a different request body.");
        this.idempotencyKey = messageOrKey;
    }

    public IdempotenciaConflictoExcepcion(String message, String idempotencyKey) {
        super(message);
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
