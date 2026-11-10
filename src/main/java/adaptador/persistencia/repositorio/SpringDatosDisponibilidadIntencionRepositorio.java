package m3.adaptador.persistencia.repositorio;

import m3.adaptador.persistencia.entidad.DisponibilidadIntencionJpaEntidad;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link DisponibilidadIntencionJpaEntidad}.
 * Do not expose this repository outside the persistence adapter.
 */
public interface SpringDatosDisponibilidadIntencionRepositorio
    extends JpaRepository<DisponibilidadIntencionJpaEntidad, UUID> {

    Optional<DisponibilidadIntencionJpaEntidad> findByDriverId(UUID driverId);

    List<DisponibilidadIntencionJpaEntidad> findByConnectedTrue();
}
