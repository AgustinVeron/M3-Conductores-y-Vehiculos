package m3.adaptador.persistencia.repositorio;

import m3.adaptador.persistencia.entidad.ConductorJpaEntidad;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDatosConductorRepositorio extends JpaRepository<ConductorJpaEntidad, UUID> {

    Optional<ConductorJpaEntidad> findByIdentitySubject(String identitySubject);

    boolean existsByIdentitySubject(String identitySubject);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM ConductorJpaEntidad d WHERE d.id = :id")
    Optional<ConductorJpaEntidad> findByIdWithPessimisticLock(@Param("id") UUID id);
}
