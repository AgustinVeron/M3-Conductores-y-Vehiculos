package m3.adaptador.api;

import m3.adaptador.api.dto.DocumentoRespuesta;
import m3.adaptador.api.dto.EnviarVehiculoDocumentoSolicitud;
import m3.aplicacion.casodeuso.RegistrarVehiculoDocumentoComando;
import m3.aplicacion.casodeuso.RegistrarVehiculoDocumentoUsoCaso;
import m3.dominio.Documento;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Vehiculo Documentation management (HU-06 / RF-3.3).
 * Contractual endpoint: /api/v1/drivers/{driverId}/vehicles/{vehicleId}/documents
 */
@RestController
@RequestMapping("/api/v1/drivers/{driverId}/vehicles/{vehicleId}/documents")
public class VehiculoDocumentoApiControlador {

    private final RegistrarVehiculoDocumentoUsoCaso registerVehicleDocumentUseCase;

    public VehiculoDocumentoApiControlador(RegistrarVehiculoDocumentoUsoCaso registerVehicleDocumentUseCase) {
        this.registerVehicleDocumentUseCase = Objects.requireNonNull(registerVehicleDocumentUseCase, "registerVehicleDocumentUseCase cannot be null");
    }

    /**
     * Registers a new revision of insurance or vehicle documentation metadata.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentoRespuesta> submitVehicleDocument(
        @PathVariable UUID driverId,
        @PathVariable UUID vehicleId,
        @Valid @RequestBody EnviarVehiculoDocumentoSolicitud request
    ) {
        RegistrarVehiculoDocumentoComando command = new RegistrarVehiculoDocumentoComando(
            driverId,
            vehicleId,
            request.documentType(),
            request.documentNumber(),
            request.issuingEntity(),
            request.issuedAt(),
            request.validUntil(),
            request.storageReference()
        );

        Documento created = registerVehicleDocumentUseCase.registerVehicleDocument(command);

        URI location = URI.create(String.format(
            "/api/v1/drivers/%s/vehicles/%s/documents/%s",
            driverId,
            vehicleId,
            created.getId()
        ));

        return ResponseEntity.created(location)
            .body(DocumentoRespuesta.fromDomain(created));
    }

    /**
     * Lists the document revision history for a vehicle belonging to the driver.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DocumentoRespuesta>> listVehicleDocuments(
        @PathVariable UUID driverId,
        @PathVariable UUID vehicleId
    ) {
        List<Documento> documents = registerVehicleDocumentUseCase.listVehicleDocuments(driverId, vehicleId);
        List<DocumentoRespuesta> response = documents.stream()
            .map(DocumentoRespuesta::fromDomain)
            .toList();

        return ResponseEntity.ok(response);
    }
}

