package m3.dominio;

/**
 * Estados posibles del conductor en el proceso de habilitación según las reglas de negocio de M3.
 */
public enum ConductorEstado {
    /**
     * Registro inicial realizado, pendiente de revisión administrativa por un operador.
     */
    PENDING("Pendiente de Revisión"),

    /**
     * Conductor aprobado y formalmente habilitado para operar.
     */
    APPROVED("Habilitado / Aprobado"),

    /**
     * Documentación o perfil observado con motivos específicos que requieren subsanación.
     */
    OBSERVED("Observado"),

    /**
     * Solicitud de habilitación rechazada por el operador con motivo fundamentado.
     */
    REJECTED("Rechazado"),

    /**
     * Conductor suspendido preventivamente o por sanción/infracción administrativa.
     */
    SUSPENDED("Suspendido");

    private final String description;

    ConductorEstado(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOperable() {
        return this == APPROVED;
    }
}
