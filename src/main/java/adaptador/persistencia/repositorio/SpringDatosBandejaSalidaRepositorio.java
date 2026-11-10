package m3.adaptador.persistencia.repositorio;

import m3.adaptador.persistencia.entidad.BandejaSalidaJpaEntidad;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for {@link BandejaSalidaJpaEntidad}. */
public interface SpringDatosBandejaSalidaRepositorio
    extends JpaRepository<BandejaSalidaJpaEntidad, UUID> {

    /** Used by the worker to pick up pending messages for M4. */
    List<BandejaSalidaJpaEntidad> findTop20ByStatusInOrderByNextAttemptAtAsc(List<String> statuses);
}
