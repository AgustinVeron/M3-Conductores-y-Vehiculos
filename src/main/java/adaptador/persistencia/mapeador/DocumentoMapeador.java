package m3.adaptador.persistencia.mapeador;

import m3.adaptador.persistencia.entidad.DocumentoJpaEntidad;
import m3.dominio.Documento;
import org.springframework.stereotype.Component;

@Component
public class DocumentoMapeador {

    public Documento toDomain(DocumentoJpaEntidad entity) {
        if (entity == null) return null;
        return new Documento(
                entity.getId(),
                entity.getDriverId(),
                entity.getVehicleId(),
                entity.getDocumentType(),
                entity.getDocumentNumber(),
                entity.getIssuingEntity(),
                entity.getIssuedAt(),
                entity.getValidUntil(),
                entity.getStorageReference(),
                entity.getRevision(),
                entity.getStatus(),
                entity.isCurrent(),
                entity.getDriverVersion(),
                entity.getRejectionReason(),
                entity.getReviewedAt(),
                entity.getReviewedBy(),
                entity.getCreatedAt(),
                entity.getServiceTypes()
        );
    }

    public DocumentoJpaEntidad toJpaEntity(Documento domain) {
        if (domain == null) return null;
        DocumentoJpaEntidad entity = new DocumentoJpaEntidad(
                domain.getId(),
                domain.getDriverId(),
                domain.getVehicleId(),
                domain.getDocumentType(),
                domain.getDocumentNumber(),
                domain.getIssuingEntity(),
                domain.getIssuedAt(),
                domain.getValidUntil(),
                domain.getStorageReference(),
                domain.getRevision(),
                domain.getStatus(),
                domain.isCurrent(),
                domain.getDriverVersion(),
                domain.getRejectionReason(),
                domain.getReviewedAt(),
                domain.getReviewedBy(),
                domain.getCreatedAt()
        );
        entity.setServiceTypes(domain.getServiceTypes());
        return entity;
    }
}
