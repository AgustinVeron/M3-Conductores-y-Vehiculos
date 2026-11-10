package m3.adaptador.seguridad;
import m3.aplicacion.puerto.salida.ActualPrincipalPuerto;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;
@Component
public class SeguridadActualPrincipalAdaptador implements ActualPrincipalPuerto {
    private final DesarrolloModo mode;
    @Value("${app.security.mock-subject:}") private String mockSubject;
    @Value("${app.security.mock-roles:ROLE_DRIVER,ROLE_OPERATOR}") private String mockRoles;
    public SeguridadActualPrincipalAdaptador(DesarrolloModo mode) { this.mode=mode; }
    private Authentication authentication() {
        var a=SecurityContextHolder.getContext().getAuthentication();
        return a!=null && a.isAuthenticated() && !"anonymousUser".equals(a.getPrincipal()) ? a : null;
    }
    private jakarta.servlet.http.HttpServletRequest request() {
        var attrs=RequestContextHolder.getRequestAttributes(); return attrs instanceof ServletRequestAttributes a ? a.getRequest():null;
    }
    public Optional<String> getSubject() {
        Authentication a=authentication();
        if(a instanceof JwtAuthenticationToken jwt) return Optional.ofNullable(jwt.getToken().getSubject());
        if(a!=null && a.getPrincipal() instanceof OidcUser user) return Optional.ofNullable(user.getSubject());
        if(a!=null) return Optional.ofNullable(a.getName());
        if(!mode.enabled()) return Optional.empty();
        var request=request();
        if(request!=null) {
            String header=request.getHeader("X-Dev-Subject");
            if(header!=null && !header.isBlank()) return Optional.of(header.trim());
            var session=request.getSession(false);
            if(session!=null && session.getAttribute("SIMULATED_SUBJECT") instanceof String subject && !subject.isBlank()) return Optional.of(subject);
        }
        return Optional.ofNullable(mockSubject).filter(s->!s.isBlank());
    }
    public boolean hasRole(String role) {
        if(role==null) return false;
        Authentication a=authentication();
        if(a!=null) return a.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(r->matches(r,role));
        if(!mode.enabled()) return false;
        var request=request(); String roles=request!=null?request.getHeader("X-Dev-Roles"):null;
        if(roles==null) roles=mockRoles;
        return roles!=null && Arrays.stream(roles.split(",")).anyMatch(r->matches(r.trim(),role));
    }
    private boolean matches(String actual,String expected) {
        return actual.equalsIgnoreCase(expected) || actual.replaceFirst("^ROLE_", "").equalsIgnoreCase(expected.replaceFirst("^ROLE_", ""));
    }
    public boolean isAuthenticated() { return getSubject().isPresent(); }
}
