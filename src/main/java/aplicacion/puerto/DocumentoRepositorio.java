package m3.aplicacion.puerto;

import m3.dominio.Documento;
import m3.dominio.DocumentoTipo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoRepositorio {
    Documento save(Documento document);
    Optional<Documento> findById(UUID id);
    Optional<Documento> findCurrentByVehicleIdAndType(UUID vehicleId, DocumentoTipo documentType);
    List<Documento> findByVehicleIdOrderByRevisionDesc(UUID vehicleId);
    Optional<Documento> findCurrentLicenseByDriverId(UUID driverId);
    List<Documento> findLicensesByDriverId(UUID driverId);
}
