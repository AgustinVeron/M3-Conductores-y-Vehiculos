package m3.adaptador.persistencia;

import m3.adaptador.persistencia.entidad.ConductorJpaEntidad;
import m3.adaptador.persistencia.repositorio.SpringDatosConductorRepositorio;
import m3.adaptador.persistencia.repositorio.SpringDatosVehiculoRepositorio;
import m3.aplicacion.puerto.ConductorRepositorio;
import m3.aplicacion.puerto.salida.ConductorRepositorioPuerto;
import m3.dominio.Conductor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia que implementa el puerto de repositorio para Conductor.
 * Conecta los casos de uso con Spring Data JPA y aísla el modelo de dominio.
 */
@Repository
public class ConductorPersistenciaAdaptador implements ConductorRepositorio, ConductorRepositorioPuerto {

    private final SpringDatosConductorRepositorio springDataRepository;
    private final ConductorMapeador driverMapper;
    private final SpringDatosVehiculoRepositorio vehicleRepository;

    @Autowired
    public ConductorPersistenciaAdaptador(SpringDatosConductorRepositorio springDataRepository,
                                  ConductorMapeador driverMapper,
                                  @Autowired(required = false) SpringDatosVehiculoRepositorio vehicleRepository) {
        this.springDataRepository = Objects.requireNonNull(springDataRepository, "springDataRepository es requerido");
        this.driverMapper = Objects.requireNonNull(driverMapper, "driverMapper es requerido");
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Conductor> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(driverMapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<Conductor> findByIdWithPessimisticLock(UUID id) {
        return springDataRepository.findByIdWithPessimisticLock(id)
                .map(driverMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Conductor> findByIdentitySubject(String identitySubject) {
        return springDataRepository.findByIdentitySubject(identitySubject)
                .map(driverMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdentitySubject(String identitySubject) {
        return springDataRepository.existsByIdentitySubject(identitySubject);
    }

    @Override
    @Transactional
    public Conductor save(Conductor driver) {
        ConductorJpaEntidad entity = driverMapper.toJpaEntity(driver);
        ConductorJpaEntidad savedEntity = springDataRepository.save(entity);
        return driverMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conductor> findAll() {
        return springDataRepository.findAll().stream()
                .map(driverMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID driverId) {
        return springDataRepository.existsById(driverId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isApproved(UUID driverId) {
        return springDataRepository.findById(driverId)
                .map(d -> "APPROVED".equalsIgnoreCase(d.getQualificationStatus()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOwnQualifiedVehicle(UUID driverId, UUID vehicleId) {
        if (vehicleRepository == null) {
            return true;
        }
        return vehicleRepository.findById(vehicleId)
                .map(v -> v.getDriverId().equals(driverId) &&
                          "APPROVED".equalsIgnoreCase(v.getQualificationStatus()))
                .orElse(false);
    }
}
