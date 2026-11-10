package m3.adaptador.persistencia.repositorio;

import m3.adaptador.persistencia.entidad.VehiculoJpaEntidad;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for VehiculoJpaEntidad.
 */
public interface SpringDatosVehiculoRepositorio extends JpaRepository<VehiculoJpaEntidad, UUID> {

    List<VehiculoJpaEntidad> findByDriverIdOrderByCreatedAtDesc(UUID driverId);

    Optional<VehiculoJpaEntidad> findByIdAndDriverId(UUID id, UUID driverId);

    boolean existsByIdAndDriverId(UUID id, UUID driverId);

    List<VehiculoJpaEntidad> findByDriverId(UUID driverId);

    boolean existsByDriverId(UUID driverId);
}
