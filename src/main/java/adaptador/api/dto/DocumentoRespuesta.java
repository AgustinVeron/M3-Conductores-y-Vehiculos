package m3.adaptador.api.dto;

import m3.dominio.Documento;
import m3.dominio.DocumentoEstado;
import m3.dominio.DocumentoTipo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record DocumentoRespuesta(
        UUID id,
        UUID driverId,
        UUID vehicleId,
        DocumentoTipo documentType,
        @JsonProperty("kind") DocumentoTipo kind,
        String documentNumber,
        String issuingEntity,
        int revision,
        DocumentoEstado status,
        @JsonProperty("reviewStatus") DocumentoEstado reviewStatus,
        Instant issuedAt,
        Instant validUntil,
        @JsonProperty("isCurrent") boolean isCurrent,
        String storageReference,
        Long driverVersion,
        Instant createdAt
) {
    public static DocumentoRespuesta fromDomain(Documento document) {
        if (document == null) return null;
        return new DocumentoRespuesta(
                document.getId(),
                document.getDriverId(),
                document.getVehicleId(),
                document.getDocumentType(),
                document.getDocumentType(),
                document.getDocumentNumber(),
                document.getIssuingEntity(),
                document.getRevision(),
                document.getStatus(),
                document.getStatus(),
                document.getIssuedAt(),
                document.getValidUntil(),
                document.isCurrent(),
                document.getStorageReference(),
                document.getDriverVersion(),
                document.getCreatedAt()
        );
    }
}