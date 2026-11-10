package m3.dominio;

import java.util.Objects;

/**
 * Value Object inmutable que representa la situación de habilitación del conductor,
 * incluyendo su estado y el motivo fundamentado en caso de observación, rechazo o suspensión.
 */
public final class HabilitacionInformacion {
    private final ConductorEstado status;
    private final String reason;

    public HabilitacionInformacion(ConductorEstado status, String reason) {
        if (status == null) {
            throw new IllegalArgumentException("El estado de habilitación no puede ser nulo");
        }
        if ((status == ConductorEstado.OBSERVED || status == ConductorEstado.REJECTED || status == ConductorEstado.SUSPENDED)
                && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("Los estados OBSERVED, REJECTED o SUSPENDED requieren un motivo descriptivo");
        }
        this.status = status;
        this.reason = reason != null ? reason.trim() : null;
    }

    public static HabilitacionInformacion pending() {
        return new HabilitacionInformacion(ConductorEstado.PENDING, "Perfil registrado, pendiente de revisión administrativa");
    }

    public static HabilitacionInformacion approved() {
        return new HabilitacionInformacion(ConductorEstado.APPROVED, null);
    }

    public static HabilitacionInformacion observed(String reason) {
        return new HabilitacionInformacion(ConductorEstado.OBSERVED, reason);
    }

    public static HabilitacionInformacion rejected(String reason) {
        return new HabilitacionInformacion(ConductorEstado.REJECTED, reason);
    }

    public static HabilitacionInformacion suspended(String reason) {
        return new HabilitacionInformacion(ConductorEstado.SUSPENDED, reason);
    }

    public ConductorEstado getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public boolean isOperable() {
        return status.isOperable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabilitacionInformacion that = (HabilitacionInformacion) o;
        return status == that.status && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, reason);
    }

    @Override
    public String toString() {
        return "HabilitacionInformacion{" +
                "status=" + status +
                ", reason='" + reason + '\'' +
                '}';
    }
}
