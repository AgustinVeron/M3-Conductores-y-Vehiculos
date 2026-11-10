package m3.adaptador.api;

import m3.dominio.Conductor;
import m3.dominio.ConductorEstado;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta para la consulta de perfil de conductor (HU-02).
 * Expone la información necesaria sin filtrar detalles internos de persistencia.
 */
public record ConductorPerfilRespuesta(
        UUID id,
        String identitySubject,
        PersonalInformacionRespuesta personalInfo,
        ConductorEstado status,
        String statusDescription,
        String qualificationReason,
        boolean operable,
        int version,
        Instant createdAt,
        Instant updatedAt
) {
    public static ConductorPerfilRespuesta from(Conductor driver) {
        if (driver == null) return null;
        return new ConductorPerfilRespuesta(
                driver.getId(),
                driver.getIdentitySubject(),
                PersonalInformacionRespuesta.from(driver.getPersonalInfo()),
                driver.getStatus(),
                driver.getStatus().getDescription(),
                driver.getQualificationReason(),
                driver.isOperable(),
                driver.getVersion(),
                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }
}
