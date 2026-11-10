package m3.adaptador.seguridad;

import java.util.Collections;
import java.util.Set;

/**
 * Contexto de seguridad inmutable que contiene la información esencial del sujeto autenticado en M3.
 */
public record PrincipalContexto(
        String subject,
        String email,
        String name,
        Set<String> roles
) {
    public PrincipalContexto {
        roles = roles != null ? Set.copyOf(roles) : Collections.emptySet();
    }

    public boolean hasRole(String role) {
        if (role == null) return false;
        if (roles.contains(role)) return true;
        // Compatibilidad con o sin prefijo ROLE_
        if (role.startsWith("ROLE_") && roles.contains(role.substring(5))) return true;
        if (!role.startsWith("ROLE_") && roles.contains("ROLE_" + role)) return true;
        return false;
    }
}
