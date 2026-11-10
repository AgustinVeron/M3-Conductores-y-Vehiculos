package m3.adaptador.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Creates a local-only simulated driver session when it is explicitly enabled. */
@Component
public class DemostracionAccesoAutenticacionFiltro extends OncePerRequestFilter {
    public static final String SUBJECT_ATTRIBUTE = "M3_DEMO_ACCESS_SUBJECT";

    @Value("${app.demo-access.enabled:false}")
    private boolean enabled;
    @Value("${app.demo-access.roles:ROLE_DRIVER}")
    private String roles;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        boolean simulated = false;
        var currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if (enabled && (currentAuthentication == null || currentAuthentication instanceof AnonymousAuthenticationToken)) {
            var session = request.getSession(false);
            Object value = session == null ? null : session.getAttribute(SUBJECT_ATTRIBUTE);
            if (value instanceof String subject && !subject.isBlank()) {
                var authentication = UsernamePasswordAuthenticationToken.authenticated(
                        subject, "", AuthorityUtils.commaSeparatedStringToAuthorityList(roles));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                simulated = true;
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            if (simulated) SecurityContextHolder.clearContext();
        }
    }
}
