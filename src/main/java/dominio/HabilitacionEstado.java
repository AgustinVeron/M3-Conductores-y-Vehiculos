package m3.dominio;

/**
 * Representa los estados de habilitación/calificación del conductor
 * según la sección 6.5 del documento de arquitectura.
 */
public enum HabilitacionEstado {
    PENDING,
    APPROVED,
    OBSERVED,
    REJECTED,
    SUSPENDED;

    public boolean isApproved() {
        return this == APPROVED;
    }
}
