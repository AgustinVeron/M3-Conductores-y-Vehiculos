package m3.adaptador.api;

import m3.dominio.Conductor;
import m3.dominio.HabilitacionEstado;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta para la representación de un conductor en la API REST.
 */
public record ConductorRespuesta(
        UUID id,
        String identitySubject,
        String name,
        HabilitacionEstado qualificationStatus,
        int version,
        Instant createdAt,
        Instant updatedAt
) {
    public static ConductorRespuesta fromDomain(Conductor driver) {
        return new ConductorRespuesta(
                driver.getId(),
                driver.getIdentitySubject(),
                driver.getName(),
                driver.getQualificationStatus(),
                driver.getVersion(),
                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }
}
