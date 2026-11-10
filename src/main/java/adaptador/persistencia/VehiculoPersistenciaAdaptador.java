package m3.adaptador.persistencia;

import m3.adaptador.persistencia.mapeador.VehiculoMapeador;
import m3.adaptador.persistencia.repositorio.SpringDatosVehiculoRepositorio;
import m3.aplicacion.puerto.VehiculoRepositorio;
import m3.dominio.Vehiculo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de persistencia que implementa el puerto VehiculoRepositorio.
 */
@Repository
public class VehiculoPersistenciaAdaptador implements VehiculoRepositorio {

    private final SpringDatosVehiculoRepositorio repository;
    private final VehiculoMapeador mapper;

    public VehiculoPersistenciaAdaptador(SpringDatosVehiculoRepositorio repository, VehiculoMapeador mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Vehiculo save(Vehiculo v) {
        var e = new m3.adaptador.persistencia.entidad.VehiculoJpaEntidad();
        e.setColor(v.getColor()); e.setCapacity(v.getCapacity());
        e.setServiceCategories(String.join(",", new java.util.TreeSet<>(v.getServiceCategories())));
        e.setId(v.getId()); e.setDriverId(v.getDriverId()); e.setVehicleType(v.getVehicleType());
        e.setServiceTypes(String.join(",", v.getServiceTypes())); e.setCountryCode(v.getCountryCode());
        e.setPlate(v.getPlate()); e.setMake(v.getMake()); e.setModel(v.getModel());
        e.setModelYear(v.getModelYear()); e.setQualificationStatus(v.getQualificationStatus());
        e.setDriverVersion(v.getDriverVersion()); e.setCreatedAt(v.getCreatedAt()); e.setUpdatedAt(v.getCreatedAt());
        return mapper.toDomain(repository.save(e));
    }

    @Override
    public Optional<Vehiculo> findById(UUID vehicleId) {
        return repository.findById(vehicleId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByIdAndDriverId(UUID vehicleId, UUID driverId) {
        return repository.existsByIdAndDriverId(vehicleId, driverId);
    }

    @Override
    public List<Vehiculo> findByDriverId(UUID driverId) {
        return repository.findByDriverIdOrderByCreatedAtDesc(driverId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Vehiculo> findByIdAndDriverId(UUID vehicleId, UUID driverId) {
        return repository.findByIdAndDriverId(vehicleId, driverId)
            .map(mapper::toDomain);
    }
}