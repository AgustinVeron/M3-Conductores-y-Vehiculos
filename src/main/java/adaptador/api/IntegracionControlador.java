package m3.adaptador.api;

import m3.adaptador.api.dto.LoteElegibilidadSolicitudDTO;
import m3.adaptador.api.dto.LoteElegibilidadRespuestaDTO;
import m3.adaptador.api.dto.ConductorDespachoInformacionSolicitud;
import m3.adaptador.api.dto.ConductorDespachoInformacionRespuesta;
import m3.adaptador.api.dto.ElegibilidadRespuestaDTO;
import m3.aplicacion.excepcion.ConductorNoEncontradoExcepcion;
import m3.aplicacion.puerto.entrada.VerificarConductorAptitudUsoCaso;
import m3.aplicacion.puerto.entrada.ObtenerConductorDespachoInformacionUsoCaso;
import m3.aplicacion.casodeuso.ConductorDespachoInformacion;
import m3.dominio.elegibilidad.ElegibilidadResultado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/integration")
public class IntegracionControlador {

    private final VerificarConductorAptitudUsoCaso checkDriverAptitudeUseCase;
    private final m3.aplicacion.puerto.ConductorRepositorio drivers;
    private final ObtenerConductorDespachoInformacionUsoCaso getDriverDispatchInfoUseCase;

    public IntegracionControlador(
            VerificarConductorAptitudUsoCaso checkDriverAptitudeUseCase,
            m3.aplicacion.puerto.ConductorRepositorio drivers,
            ObtenerConductorDespachoInformacionUsoCaso getDriverDispatchInfoUseCase) {
        this.drivers=drivers;
        this.checkDriverAptitudeUseCase = Objects.requireNonNull(checkDriverAptitudeUseCase);
        this.getDriverDispatchInfoUseCase = Objects.requireNonNull(getDriverDispatchInfoUseCase);
    }

    @GetMapping("/drivers/{driverId}/eligibility")
    public ResponseEntity<ElegibilidadRespuestaDTO> getEligibility(@PathVariable UUID driverId) {
        ElegibilidadResultado result = checkDriverAptitudeUseCase.execute(driverId);
        return ResponseEntity.ok(ElegibilidadRespuestaDTO.from(result));
    }

    @PostMapping("/eligibility/batch")
    public ResponseEntity<LoteElegibilidadRespuestaDTO> batchEligibility(@Valid @RequestBody LoteElegibilidadSolicitudDTO request) {
        List<LoteElegibilidadRespuestaDTO.DriverEligibilityEntry> results = new ArrayList<>();
        for (UUID id : request.driverIds()) {
            try {
                ElegibilidadResultado res = checkDriverAptitudeUseCase.execute(id);
                results.add(new LoteElegibilidadRespuestaDTO.DriverEligibilityEntry(id, ElegibilidadRespuestaDTO.from(res)));
            } catch (ConductorNoEncontradoExcepcion e) {
                results.add(new LoteElegibilidadRespuestaDTO.DriverEligibilityEntry(
                    id,
                    new ElegibilidadRespuestaDTO(false, List.of("DRIVER_NOT_FOUND"), Instant.now(), 0, Instant.now())
                ));
            }
        }
        return ResponseEntity.ok(new LoteElegibilidadRespuestaDTO(results));
    }

    /**
     * Returns the M3-owned dispatch data requested by M5. Effective availability
     * (location, assignment, offer or trip) remains exclusively in M4.
     */
    @PostMapping("/drivers/dispatch-info")
    public ResponseEntity<ConductorDespachoInformacionRespuesta> getDispatchInfo(
            @Valid @RequestBody ConductorDespachoInformacionSolicitud request) {
        ConductorDespachoInformacion result = getDriverDispatchInfoUseCase.execute(request.conductorId());
        return ResponseEntity.ok()
                .eTag(String.valueOf(result.driverVersion()))
                .body(ConductorDespachoInformacionRespuesta.from(result));
    }

    @GetMapping("/drivers/{driverId}/summary")
    public ResponseEntity<DriverSummaryDTO> getSummary(@PathVariable UUID driverId) {
        ElegibilidadResultado result = checkDriverAptitudeUseCase.execute(driverId);
        return ResponseEntity.ok(new DriverSummaryDTO(driverId, drivers.findById(driverId).orElseThrow(() -> ConductorNoEncontradoExcepcion.forId(driverId)).getDisplayName(), result.eligible()));
    }

    public record DriverSummaryDTO(UUID id, String displayName, boolean eligible) {}
}
