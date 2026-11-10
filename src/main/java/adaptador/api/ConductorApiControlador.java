package m3.adaptador.api;

import m3.aplicacion.casodeuso.CrearConductorPerfilUsoCaso;
import m3.dominio.Conductor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Controlador REST para operaciones sobre conductores.
 * Expone POST /api/v1/drivers para la creación de perfiles (HU-01 / CLON-6).
 */
@RestController
@RequestMapping("/api/v1/drivers")
public class ConductorApiControlador {

    private final CrearConductorPerfilUsoCaso createDriverProfileUseCase;

    public ConductorApiControlador(CrearConductorPerfilUsoCaso createDriverProfileUseCase) {
        this.createDriverProfileUseCase = createDriverProfileUseCase;
    }

    @PostMapping
    public ResponseEntity<ConductorRespuesta> createDriver(@Valid @RequestBody CrearConductorSolicitud request, org.springframework.security.core.Authentication authentication) {
        if(authentication!=null && authentication.isAuthenticated() && !authentication.getName().equals(request.getIdentitySubject()))
            throw new m3.aplicacion.excepcion.ConductorAccesoDenegadoExcepcion("La identidad del perfil debe coincidir con el token.");
        Conductor createdDriver = createDriverProfileUseCase.execute(
                request.getIdentitySubject(),
                request.getName()
        );

        URI location = URI.create("/api/v1/drivers/" + createdDriver.getId());
        return ResponseEntity.created(location).eTag("\""+createdDriver.getVersion()+"\"").body(ConductorRespuesta.fromDomain(createdDriver));
    }
}
