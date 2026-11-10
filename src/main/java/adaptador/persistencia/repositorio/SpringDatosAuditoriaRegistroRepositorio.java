package m3.adaptador.persistencia.repositorio;

import m3.adaptador.persistencia.entidad.AuditoriaRegistroJpaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDatosAuditoriaRegistroRepositorio extends JpaRepository<AuditoriaRegistroJpaEntidad, UUID> {
}
