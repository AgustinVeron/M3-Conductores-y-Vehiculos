package m3.adaptador.web;

import m3.aplicacion.excepcion.ConductorAccesoDenegadoExcepcion;
import m3.aplicacion.excepcion.ConductorNoEncontradoExcepcion;
import m3.aplicacion.puerto.entrada.ObtenerConductorPerfilUsoCaso;
import m3.aplicacion.puerto.entrada.ObtenerMiConductorPerfilUsoCaso;
import m3.aplicacion.puerto.entrada.ActualizacionMiPersonalInformacionUsoCaso;
import m3.adaptador.seguridad.RolNavegacion;
import m3.dominio.Conductor;
import m3.dominio.excepcion.VersionInconsistenciaExcepcion;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.core.Authentication;

/**
 * Controlador Web (Thymeleaf) para la interfaz de usuario de M3.
 * Atiende las vistas de consulta de perfil de conductor (HU-02):
 * - /ui: Inicio de la interfaz de conductor
 * - /ui/profile: Vista detallada del perfil y estado actual
 * - /ui/profile?driverId={id}: Vista con parámetro de consulta (para pruebas u operador)
 */
@Controller
@RequestMapping("/ui")
public class ConductorPerfilWebControlador {

    private final ObtenerMiConductorPerfilUsoCaso getMyDriverProfileUseCase;
    private final ObtenerConductorPerfilUsoCaso getDriverProfileUseCase;
    private final ActualizacionMiPersonalInformacionUsoCaso updateMyPersonalInfo;
    private final RolNavegacion navigation;

    public ConductorPerfilWebControlador(ObtenerMiConductorPerfilUsoCaso getMyDriverProfileUseCase,
                                      ObtenerConductorPerfilUsoCaso getDriverProfileUseCase,
                                      ActualizacionMiPersonalInformacionUsoCaso updateMyPersonalInfo,
                                      RolNavegacion navigation) {
        this.getMyDriverProfileUseCase = Objects.requireNonNull(getMyDriverProfileUseCase, "getMyDriverProfileUseCase es requerido");
        this.getDriverProfileUseCase = Objects.requireNonNull(getDriverProfileUseCase, "getDriverProfileUseCase es requerido");
        this.updateMyPersonalInfo = Objects.requireNonNull(updateMyPersonalInfo, "updateMyPersonalInfo es requerido");
        this.navigation = Objects.requireNonNull(navigation, "navigation es requerida");
    }

    @GetMapping({"", "/"})
    public String index(Authentication authentication) {
        return "redirect:" + navigation.landingPath(authentication);
    }

    @GetMapping("/profile")
    public String viewProfile(@RequestParam(name = "driverId", required = false) String driverIdParam,
                              @RequestParam(name = "switchSession", defaultValue = "false") boolean switchSession,
                              HttpServletRequest request,
                              Authentication authentication,
                              Model model) {
        if (navigation.isOperatorOnly(authentication)) {
            return "redirect:/ui/operator/drivers";
        }
        try {
            Conductor driver;
            if (driverIdParam != null && !driverIdParam.isBlank()) {
                driver = getDriverProfileUseCase.getDriverProfileById(UUID.fromString(driverIdParam));
                if (switchSession && driver != null && request != null && Boolean.TRUE.equals(request.getAttribute("m3.development"))) {
                    request.getSession().setAttribute("SIMULATED_SUBJECT", driver.getIdentitySubject());
                }
            } else {
                driver = getMyDriverProfileUseCase.getMyDriverProfile();
            }

            model.addAttribute("driver", ConductorPerfilVistaModelo.from(driver));
            return "perfil";
        } catch (ConductorNoEncontradoExcepcion ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("errorType", "NOT_FOUND");
            return "error";
        } catch (ConductorAccesoDenegadoExcepcion ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("errorType", "ACCESS_DENIED");
            return "error";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", "Identificador de conductor con formato inválido.");
            model.addAttribute("errorType", "BAD_REQUEST");
            return "error";
        }
    }

    @GetMapping("/profile/{driverId}")
    public String viewProfileByPath(@PathVariable UUID driverId, HttpServletRequest request,
                                    Authentication authentication, Model model) {
        return viewProfile(driverId.toString(), false, request, authentication, model);
    }

    @GetMapping("/profile/edit")
    public String editProfile(Authentication authentication, Model model) {
        if (navigation.isOperatorOnly(authentication)) {
            return "redirect:/ui/operator/drivers";
        }
        try {
            model.addAttribute("form", PersonalInformacionFormulario.from(getMyDriverProfileUseCase.getMyDriverProfile()));
            return "editar-perfil";
        } catch (ConductorNoEncontradoExcepcion ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("errorType", "NOT_FOUND");
            return "error";
        }
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("form") PersonalInformacionFormulario form, BindingResult bindingResult,
                                Authentication authentication, Model model, RedirectAttributes flash) {
        if (navigation.isOperatorOnly(authentication)) {
            return "redirect:/ui/operator/drivers";
        }
        if (bindingResult.hasErrors()) {
            return "editar-perfil";
        }
        try {
            updateMyPersonalInfo.updatePersonalInfo(form.toPersonalInfo(), form.getVersion());
            flash.addFlashAttribute("success", "Datos personales actualizados.");
            return "redirect:/ui/profile";
        } catch (IllegalArgumentException | VersionInconsistenciaExcepcion ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "editar-perfil";
        }
    }
}
