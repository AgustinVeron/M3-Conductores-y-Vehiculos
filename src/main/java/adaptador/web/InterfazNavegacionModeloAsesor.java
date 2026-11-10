package m3.adaptador.web;

import m3.adaptador.seguridad.RolNavegacion;
import m3.aplicacion.excepcion.ConductorNoEncontradoExcepcion;
import m3.aplicacion.puerto.entrada.ObtenerMiConductorPerfilUsoCaso;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds role-aware navigation flags to every Thymeleaf web view. */
@ControllerAdvice
public class InterfazNavegacionModeloAsesor {
    private final RolNavegacion navigation;
    private final ObjectProvider<ObtenerMiConductorPerfilUsoCaso> getMyDriverProfile;

    public InterfazNavegacionModeloAsesor(ObjectProvider<RolNavegacion> navigation,
                                   ObjectProvider<ObtenerMiConductorPerfilUsoCaso> getMyDriverProfile) {
        this.navigation = navigation.getIfAvailable(RolNavegacion::new);
        this.getMyDriverProfile = getMyDriverProfile;
    }

    @ModelAttribute
    public void addNavigation(Model model, Authentication authentication) {
        boolean hasDriverRole = navigation.hasDriverRole(authentication);
        boolean hasOperatorRole = navigation.hasOperatorRole(authentication);
        model.addAttribute("hasDriverRole", hasDriverRole);
        model.addAttribute("hasOperatorRole", hasOperatorRole);
        model.addAttribute("showCreateProfile", hasDriverRole && !hasDriverProfile());
        model.addAttribute("homePath", navigation.landingPath(authentication));
    }

    private boolean hasDriverProfile() {
        ObtenerMiConductorPerfilUsoCaso useCase = getMyDriverProfile.getIfAvailable();
        if (useCase == null) {
            return false;
        }
        try {
            useCase.getMyDriverProfile();
            return true;
        } catch (ConductorNoEncontradoExcepcion ignored) {
            return false;
        }
    }
}
