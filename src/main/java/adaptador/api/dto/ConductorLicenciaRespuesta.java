package m3.adaptador.api.dto;

import m3.dominio.Documento;
import m3.dominio.DocumentoEstado;
import m3.dominio.DocumentoTipo;
import m3.dominio.ServicioTipo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

// The public API does not disclose the license number or storage location.
public record ConductorLicenciaRespuesta(UUID id, UUID driverId, UUID vehicleId,
                                    DocumentoTipo kind, int revision,
                                    DocumentoEstado reviewStatus, Instant issuedAt,
                                    Instant validUntil, @JsonProperty("isCurrent") boolean isCurrent,
                                    Set<ServicioTipo> serviceTypes, long driverVersion,
                                    Instant createdAt) {
    public static ConductorLicenciaRespuesta from(Documento document) {
        return new ConductorLicenciaRespuesta(document.getId(), document.getDriverId(), null,
                document.getDocumentType(), document.getRevision(), document.getStatus(),
                document.getIssuedAt(), document.getValidUntil(), document.isCurrent(),
                document.getServiceTypes(), document.getDriverVersion(), document.getCreatedAt());
    }
}
