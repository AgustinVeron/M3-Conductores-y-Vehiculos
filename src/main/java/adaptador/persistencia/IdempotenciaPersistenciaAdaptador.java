package m3.adaptador.persistencia;

import m3.adaptador.persistencia.entidad.IdempotenciaRegistroJpaEntidad;
import m3.adaptador.persistencia.repositorio.SpringDatosIdempotenciaRepositorio;
import m3.aplicacion.puerto.IdempotenciaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class IdempotenciaPersistenciaAdaptador implements IdempotenciaRepositorio {

    private final SpringDatosIdempotenciaRepositorio repository;

    public IdempotenciaPersistenciaAdaptador(SpringDatosIdempotenciaRepositorio repository) {
        this.repository = repository;
    }

    @Override
    public Optional<m3.dominio.IdempotenciaRegistro> findByKeyAndMethodAndPath(String key, String method, String path) {
        return repository.findByIdempotencyKeyAndHttpMethodAndRequestPath(key, method, path)
                .map(entity -> new m3.dominio.IdempotenciaRegistro(
                        entity.getId(),
                        entity.getIdempotencyKey(),
                        entity.getPrincipal(),
                        entity.getHttpMethod(),
                        entity.getRequestPath(),
                        entity.getRequestHash(),
                        entity.getResponseStatus(),
                        entity.getResponseBody(),
                        entity.getCreatedAt()
                ));
    }

    @Override
    public void save(m3.dominio.IdempotenciaRegistro record) {
        IdempotenciaRegistroJpaEntidad entity = new IdempotenciaRegistroJpaEntidad(
                record.getId(),
                record.getIdempotencyKey(),
                record.getPrincipal(),
                record.getHttpMethod(),
                record.getRequestPath(),
                record.getRequestHash(),
                record.getResponseStatus(),
                record.getResponseBody(),
                record.getCreatedAt()
        );
        repository.save(entity);
    }
}
