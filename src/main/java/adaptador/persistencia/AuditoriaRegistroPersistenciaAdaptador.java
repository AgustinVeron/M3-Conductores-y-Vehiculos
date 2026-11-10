package m3.adaptador.persistencia;

import m3.adaptador.persistencia.entidad.AuditoriaRegistroJpaEntidad;
import m3.adaptador.persistencia.repositorio.SpringDatosAuditoriaRegistroRepositorio;
import m3.aplicacion.puerto.AuditoriaRegistroRepositorio;
import m3.dominio.AuditoriaRegistro;
import org.springframework.stereotype.Repository;

@Repository
public class AuditoriaRegistroPersistenciaAdaptador implements AuditoriaRegistroRepositorio {

    private final SpringDatosAuditoriaRegistroRepositorio repository;

    public AuditoriaRegistroPersistenciaAdaptador(SpringDatosAuditoriaRegistroRepositorio repository) {
        this.repository = repository;
    }

    @Override
    public void save(AuditoriaRegistro auditLog) {
        AuditoriaRegistroJpaEntidad entity = new AuditoriaRegistroJpaEntidad(
                auditLog.getId(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDriverId(),
                auditLog.getAction(),
                auditLog.getActor(),
                auditLog.getPreviousStatus(),
                auditLog.getNewStatus(),
                auditLog.getReason(),
                auditLog.getCorrelationId(),
                auditLog.getCreatedAt()
        );
        repository.save(entity);
    }
}
