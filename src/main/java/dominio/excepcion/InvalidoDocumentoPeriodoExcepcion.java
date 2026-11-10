package m3.dominio.excepcion;

import java.time.Instant;

/**
 * Thrown when the document validity dates violate business rules (issuedAt must be strictly before validUntil).
 */
public class InvalidoDocumentoPeriodoExcepcion extends RuntimeException {

    private final Instant issuedAt;
    private final Instant validUntil;

    public InvalidoDocumentoPeriodoExcepcion(Instant issuedAt, Instant validUntil) {
        super(String.format("Invalid document period: issuedAt (%s) must be strictly before validUntil (%s)", issuedAt, validUntil));
        this.issuedAt = issuedAt;
        this.validUntil = validUntil;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getValidUntil() {
        return validUntil;
    }
}

