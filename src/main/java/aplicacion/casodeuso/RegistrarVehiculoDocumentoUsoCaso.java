package m3.aplicacion.casodeuso;

import m3.dominio.Documento;
import java.util.List;
import java.util.UUID;

/**
 * Use case input port for registering and querying vehicle documents (HU-06).
 */
public interface RegistrarVehiculoDocumentoUsoCaso {

    /**
     * Registers a new revision of a vehicle document.
     * Previous current document of the same type is marked non-current,
     * and the new document is saved as current with status PENDING.
     */
    Documento registerVehicleDocument(RegistrarVehiculoDocumentoComando command);

    /**
     * Lists the document revision history for a vehicle belonging to the specified driver.
     */
    List<Documento> listVehicleDocuments(UUID driverId, UUID vehicleId);
}

