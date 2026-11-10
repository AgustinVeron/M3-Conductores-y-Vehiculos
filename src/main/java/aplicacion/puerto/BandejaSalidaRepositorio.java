package m3.aplicacion.puerto;

import m3.dominio.DisponibilidadIntencion;

/**
 * Output port for the transactional HTTP outbox (E1 pattern).
 *
 * <p>An outbox entry is persisted in the same transaction as the
 * availability intent so no message is ever lost due to a crash
 * between commit and the actual HTTP call to M4.
 */
public interface BandejaSalidaRepositorio {

    /**
     * Saves a pending outbox entry for the M4 availability-intent call.
     *
     * @param intent        The intent whose data will be sent to M4.
     * @param correlationId The request correlation id to include in the payload.
     */
    void enqueue(DisponibilidadIntencion intent, String correlationId);
}
