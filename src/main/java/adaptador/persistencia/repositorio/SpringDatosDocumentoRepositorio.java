package m3.adaptador.persistencia.repositorio;

import m3.adaptador.persistencia.entidad.DocumentoJpaEntidad;
import m3.dominio.DocumentoTipo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for DocumentoJpaEntidad.
 */
public interface SpringDatosDocumentoRepositorio extends JpaRepository<DocumentoJpaEntidad, UUID> {

    Optional<DocumentoJpaEntidad> findByVehicleIdAndDocumentTypeAndCurrentTrue(UUID vehicleId, DocumentoTipo documentType);

    List<DocumentoJpaEntidad> findByVehicleIdOrderByRevisionDesc(UUID vehicleId);

    Optional<DocumentoJpaEntidad> findByDriverIdAndDocumentTypeAndVehicleIdIsNullAndCurrentTrue(UUID driverId, DocumentoTipo type);

    List<DocumentoJpaEntidad> findByDriverIdAndDocumentTypeAndVehicleIdIsNullOrderByRevisionDesc(UUID driverId, DocumentoTipo type);
}
