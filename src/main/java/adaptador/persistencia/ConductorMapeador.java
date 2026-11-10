package m3.adaptador.persistencia;

import m3.adaptador.persistencia.entidad.ConductorJpaEntidad;
import m3.dominio.Conductor;
import m3.dominio.ConductorEstado;
import m3.dominio.PersonalInformacion;
import m3.dominio.HabilitacionInformacion;
import org.springframework.stereotype.Component;

/**
 * Mapper bidireccional entre la entidad de dominio pura Conductor y la entidad JPA ConductorJpaEntidad.
 */
@Component
public class ConductorMapeador {

    public Conductor toDomain(ConductorJpaEntidad entity) {
        if (entity == null) {
            return null;
        }

        String full=entity.getDisplayName().trim();
        String first=entity.getFirstName()!=null && !entity.getFirstName().isBlank() ? entity.getFirstName() : full;
        String last=entity.getFirstName()!=null && !entity.getFirstName().isBlank() ? entity.getLastName() : "";
        PersonalInformacion personalInfo=PersonalInformacion.fromStored(first,last,entity.getDni(),entity.getBirthDate(),
                entity.getAddress(),entity.getPhone(),entity.getEmail());

        ConductorEstado status;
        try {
            status = entity.getQualificationStatus() != null
                    ? ConductorEstado.valueOf(entity.getQualificationStatus())
                    : ConductorEstado.PENDING;
        } catch (IllegalArgumentException e) {
            status = ConductorEstado.PENDING;
        }

        HabilitacionInformacion qualification = new HabilitacionInformacion(status, entity.getQualificationReason());

        return new Conductor(
                entity.getId(),
                entity.getIdentitySubject(),
                personalInfo,
                qualification,
                entity.getVersion() != null ? entity.getVersion().intValue() : 1,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public ConductorJpaEntidad toJpaEntity(Conductor domain) {
        if (domain == null) {
            return null;
        }

        ConductorJpaEntidad entity = new ConductorJpaEntidad();
        entity.setId(domain.getId());
        entity.setIdentitySubject(domain.getIdentitySubject());
        entity.setDisplayName(domain.getDisplayName());
        entity.setQualificationStatus(domain.getQualificationStatus() != null
                ? domain.getQualificationStatus().name()
                : "PENDING");
        entity.setQualificationReason(domain.getQualificationReason());
        entity.setVersion((long) domain.getVersion());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        PersonalInformacion info = domain.getPersonalInfo();
        if (info != null) {
            entity.setFirstName(info.getFirstName());
            entity.setLastName(info.getLastName());
            entity.setDni(info.getDni());
            entity.setBirthDate(info.getBirthDate());
            entity.setAddress(info.getAddress());
            entity.setPhone(info.getPhone());
            entity.setEmail(info.getEmail());
        }

        return entity;
    }
}
