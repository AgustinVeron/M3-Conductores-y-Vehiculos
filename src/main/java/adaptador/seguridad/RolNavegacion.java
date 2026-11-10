package m3.adaptador.seguridad;

import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/** Decides the web areas available to the authenticated M1 identity. */
@Component
public class RolNavegacion {
    private static final Set<String> DRIVER_ROLES = Set.of("ROLE_DRIVER", "ROLE_CONDUCTOR");
    private static final Set<String> OPERATOR_ROLES = Set.of(
            "ROLE_OPERATOR", "ROLE_OPERADOR", "ROLE_ADMIN", "ROLE_ADMINISTRADOR");

    public boolean hasDriverRole(Authentication authentication) {
        return hasAnyRole(authentication, DRIVER_ROLES);
    }

    public boolean hasOperatorRole(Authentication authentication) {
        return hasAnyRole(authentication, OPERATOR_ROLES);
    }

    public boolean isOperatorOnly(Authentication authentication) {
        return hasOperatorRole(authentication) && !hasDriverRole(authentication);
    }

    public String landingPath(Authentication authentication) {
        return isOperatorOnly(authentication) ? "/ui/operator/drivers" : "/ui/profile";
    }

    private boolean hasAnyRole(Authentication authentication, Set<String> expectedRoles) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(expectedRoles::contains);
    }
}
