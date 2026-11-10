package m3.adaptador.persistencia.repositorio;

import m3.adaptador.persistencia.entidad.IdempotenciaRegistroJpaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDatosIdempotenciaRepositorio extends JpaRepository<IdempotenciaRegistroJpaEntidad, UUID> {

    Optional<IdempotenciaRegistroJpaEntidad> findByIdempotencyKeyAndHttpMethodAndRequestPath(
            String idempotencyKey, String httpMethod, String requestPath);
}
