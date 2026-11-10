package m3.dominio.elegibilidad;

import m3.dominio.DocumentoTipo;
import m3.dominio.HabilitacionEstado;

import java.time.Instant;
import java.util.UUID;

public record DocumentoInstantanea(
        UUID id,
        DocumentoTipo type,
        HabilitacionEstado qualificationStatus,
        Instant validUntil, Instant issuedAt, java.util.Set<m3.dominio.ServicioTipo> serviceTypes
) {
    public boolean isExpiredAt(Instant now) {
        return validUntil != null && !now.isBefore(validUntil);
    }
}
