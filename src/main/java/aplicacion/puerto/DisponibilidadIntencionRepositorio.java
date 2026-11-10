package m3.aplicacion.puerto;

import m3.dominio.DisponibilidadIntencion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port: persistence for {@link DisponibilidadIntencion}.
 * Implemented by the persistence adapter (JPA).
 */
public interface DisponibilidadIntencionRepositorio {

    /**
     * Finds the current intent for the given driver, if any.
     */
    Optional<DisponibilidadIntencion> findByDriverId(UUID driverId);

    /** Returns the persisted intents for drivers who are currently connected. */
    default List<DisponibilidadIntencion> findConnected() {
        return List.of();
    }

    /**
     * Persists a new or updated availability intent.
     *
     * @return The saved intent (may have DB-generated fields).
     */
    DisponibilidadIntencion save(DisponibilidadIntencion intent);
}
