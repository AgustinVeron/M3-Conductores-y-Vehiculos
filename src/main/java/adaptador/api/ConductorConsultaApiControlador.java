package m3.adaptador.api;

import m3.aplicacion.puerto.entrada.ObtenerConductorPerfilUsoCaso;
import m3.aplicacion.puerto.entrada.ObtenerMiConductorPerfilUsoCaso;
import m3.dominio.Conductor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/**
 * Controlador REST que expone los endpoints de consulta de perfil de conductor (HU-02).
 * - GET /api/v1/me/driver: Conductor autenticado consulta su propio perfil.
 * - GET /api/v1/drivers/{driverId}: Consulta por ID (propietario u operador).
 *
 * Cumple con el contrato OpenAPI 3.0.3 y las normas de arquitectura hexagonal.
 */
@RestController
@RequestMapping("/api/v1")
public class ConductorConsultaApiControlador {

    private final ObtenerConductorPerfilUsoCaso getDriverProfileUseCase;
    private final ObtenerMiConductorPerfilUsoCaso getMyDriverProfileUseCase;

    public ConductorConsultaApiControlador(ObtenerConductorPerfilUsoCaso getDriverProfileUseCase,
                                     ObtenerMiConductorPerfilUsoCaso getMyDriverProfileUseCase) {
        this.getDriverProfileUseCase = Objects.requireNonNull(getDriverProfileUseCase, "getDriverProfileUseCase es requerido");
        this.getMyDriverProfileUseCase = Objects.requireNonNull(getMyDriverProfileUseCase, "getMyDriverProfileUseCase es requerido");
    }

    /**
     * HU-02: Consultar mi perfil como conductor.
     * Resuelve la identidad del conductor mediante el contexto de seguridad autenticado.
     */
    @GetMapping("/me/driver")
    public ResponseEntity<ConductorPerfilRespuesta> getMyDriverProfile() {
        Conductor driver = getMyDriverProfileUseCase.getMyDriverProfile();
        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, "\"" + driver.getVersion() + "\"")
                .body(ConductorPerfilRespuesta.from(driver));
    }

    /**
     * HU-02: Consultar perfil de conductor por identificador único.
     * Permitido al propietario del registro o a un operador administrativo.
     */
    @GetMapping("/drivers/{driverId}")
    public ResponseEntity<ConductorPerfilRespuesta> getDriverProfileById(@PathVariable UUID driverId) {
        Conductor driver = getDriverProfileUseCase.getDriverProfileById(driverId);
        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, "\"" + driver.getVersion() + "\"")
                .body(ConductorPerfilRespuesta.from(driver));
    }
}
