package m3.adaptador.persistencia;

import m3.adaptador.persistencia.entidad.DocumentoJpaEntidad;
import m3.adaptador.persistencia.mapeador.DocumentoMapeador;
import m3.adaptador.persistencia.repositorio.SpringDatosDocumentoRepositorio;
import m3.aplicacion.puerto.DocumentoRepositorio;
import m3.dominio.Documento;
import m3.dominio.DocumentoTipo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DocumentoPersistenciaAdaptador implements DocumentoRepositorio {

    private final SpringDatosDocumentoRepositorio repository;
    private final DocumentoMapeador mapper;

    public DocumentoPersistenciaAdaptador(SpringDatosDocumentoRepositorio repository, DocumentoMapeador mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Documento save(Documento document) {
        DocumentoJpaEntidad entity = mapper.toJpaEntity(document);
        // Flush the old current revision before inserting its replacement: PostgreSQL
        // checks the partial unique index at each statement, not at transaction commit.
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<Documento> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Documento> findCurrentByVehicleIdAndType(UUID vehicleId, DocumentoTipo documentType) {
        return repository.findByVehicleIdAndDocumentTypeAndCurrentTrue(vehicleId, documentType)
                .map(mapper::toDomain);
    }

    @Override
    public List<Documento> findByVehicleIdOrderByRevisionDesc(UUID vehicleId) {
        return repository.findByVehicleIdOrderByRevisionDesc(vehicleId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Documento> findCurrentLicenseByDriverId(UUID driverId) {
        return repository.findByDriverIdAndDocumentTypeAndVehicleIdIsNullAndCurrentTrue(driverId, DocumentoTipo.LICENSE)
                .map(mapper::toDomain);
    }

    @Override
    public List<Documento> findLicensesByDriverId(UUID driverId) {
        return repository.findByDriverIdAndDocumentTypeAndVehicleIdIsNullOrderByRevisionDesc(driverId, DocumentoTipo.LICENSE)
                .stream().map(mapper::toDomain).toList();
    }
}
