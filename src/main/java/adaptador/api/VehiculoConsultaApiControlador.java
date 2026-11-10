package m3.adaptador.api;

import m3.adaptador.api.dto.VehiculoPaginaRespuesta;
import m3.adaptador.api.dto.VehiculoRespuesta;
import m3.aplicacion.casodeuso.ObtenerConductorVehiculosUsoCaso;
import m3.dominio.Vehiculo;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultar vehículos del conductor (HU-04 / RF-3.2).
 */
@RestController
@RequestMapping("/api/v1/drivers/{driverId}/vehicles")
public class VehiculoConsultaApiControlador {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";
    private final ObtenerConductorVehiculosUsoCaso getDriverVehiclesUseCase;

    public VehiculoConsultaApiControlador(ObtenerConductorVehiculosUsoCaso getDriverVehiclesUseCase) {
        this.getDriverVehiclesUseCase = Objects.requireNonNull(getDriverVehiclesUseCase, "getDriverVehiclesUseCase cannot be null");
    }

    /**
     * Lista todos los vehículos de un conductor.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehiculoPaginaRespuesta> listVehicles(
        @PathVariable UUID driverId,
        HttpServletRequest request
    ) {
        List<Vehiculo> domainVehicles = getDriverVehiclesUseCase.listVehiclesByDriver(driverId);
        List<VehiculoRespuesta> items = domainVehicles.stream()
            .map(VehiculoRespuesta::fromDomain)
            .toList();

        String correlationId = resolveCorrelationId(request);

        return ResponseEntity.ok()
            .header(CORRELATION_HEADER, correlationId)
            .body(new VehiculoPaginaRespuesta(items, null));
    }

    /**
     * Consulta el detalle de un vehículo puntual de un conductor.
     */
    @GetMapping(path = "/{vehicleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehiculoRespuesta> getVehicle(
        @PathVariable UUID driverId,
        @PathVariable UUID vehicleId,
        HttpServletRequest request
    ) {
        Vehiculo vehicle = getDriverVehiclesUseCase.getVehicle(driverId, vehicleId);
        String correlationId = resolveCorrelationId(request);

        return ResponseEntity.ok()
            .header(CORRELATION_HEADER, correlationId)
            .header("ETag", "\"" + vehicle.getDriverVersion() + "\"")
            .body(VehiculoRespuesta.fromDomain(vehicle));
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }
}