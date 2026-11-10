package m3.aplicacion.casodeuso;

import m3.dominio.DocumentoTipo;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Command representing the input data needed to register a new vehicle document revision.
 */
public record RegistrarVehiculoDocumentoComando(
    UUID driverId,
    UUID vehicleId,
    DocumentoTipo documentType,
    String documentNumber,
    String issuingEntity,
    Instant issuedAt,
    Instant validUntil,
    String storageReference
) {
    public RegistrarVehiculoDocumentoComando {
        Objects.requireNonNull(driverId, "driverId must not be null");
        Objects.requireNonNull(vehicleId, "vehicleId must not be null");
        Objects.requireNonNull(documentType, "documentType must not be null");
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new IllegalArgumentException("documentNumber must not be null or blank");
        }
        Objects.requireNonNull(issuedAt, "issuedAt must not be null");
        Objects.requireNonNull(validUntil, "validUntil must not be null");
    }
}

