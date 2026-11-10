package m3.aplicacion.casodeuso;

/**
 * Input port for the "Request Availability" use case (HU-10).
 */
public interface SolicitudDisponibilidadUsoCaso {

    /**
     * Validates, persists and enqueues the driver's availability intent.
     *
     * @param command The command carrying driver intent and idempotency data.
     * @return The result ready for the REST controller to build a 202 response.
     */
    SolicitudDisponibilidadResultado execute(SolicitudDisponibilidadComando command);
}
