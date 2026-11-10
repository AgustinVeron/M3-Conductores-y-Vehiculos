package m3.dominio;

public enum RevisionDecision {
    APPROVED,
    OBSERVED,
    REJECTED;

    public boolean requiresReason() {
        return this != APPROVED;
    }

    public String missingReasonMessage() {
        return switch (this) {
            case OBSERVED -> "Debe indicar el motivo de la observación.";
            case REJECTED -> "Debe indicar el motivo del rechazo.";
            case APPROVED -> "";
        };
    }
}
