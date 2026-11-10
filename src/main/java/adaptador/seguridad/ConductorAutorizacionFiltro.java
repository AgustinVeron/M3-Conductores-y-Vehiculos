package m3.adaptador.seguridad;
import m3.aplicacion.casodeuso.ConductorAccesoServicio;
import m3.aplicacion.puerto.salida.ActualPrincipalPuerto;
import m3.aplicacion.excepcion.ConductorAccesoDenegadoExcepcion;
import m3.dominio.excepcion.ConductorNoEncontradoExcepcion;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.web.filter.OncePerRequestFilter;
public class ConductorAutorizacionFiltro extends OncePerRequestFilter {
    private final ConductorAccesoServicio access; private final ActualPrincipalPuerto principal; private final DesarrolloModo mode;
    private static final Pattern DRIVER=Pattern.compile("^/(?:api/v1|ui)/(?:operator/)?drivers/([0-9a-fA-F-]{36})(?:/.*)?$");
    public ConductorAutorizacionFiltro(ConductorAccesoServicio access,ActualPrincipalPuerto principal,DesarrolloModo mode) { this.access=access;this.principal=principal;this.mode=mode; }
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        String path=request.getRequestURI(); request.setAttribute("m3.development",mode.enabled());
        if(!path.startsWith("/api/") && !path.startsWith("/ui")) { chain.doFilter(request,response);return; }
        // Development-only fixtures may intentionally exercise legacy controllers without an identity.
        if(mode.enabled() && !principal.isAuthenticated()) { chain.doFilter(request,response);return; }
        try {
            if(path.startsWith("/api/v1/integration/")) {
                String scope;
                if(path.endsWith("/m6/trips/completed")) scope="SCOPE_m6:trip:completed:write";
                else if(path.endsWith("/m5/offers")) scope="SCOPE_m5:offers:write";
                else if(path.endsWith("/summary")) scope="SCOPE_m3:drivers:read";
                else scope="SCOPE_m3:eligibility:read";
                if(!principal.hasRole(scope) && !access.operator()) throw new ConductorAccesoDenegadoExcepcion("Scope de integración insuficiente.");
            } else {
                if(!mode.enabled() && !(principal.hasRole("DRIVER") || principal.hasRole("CONDUCTOR") || access.operator()))
                    throw new ConductorAccesoDenegadoExcepcion("La operación requiere un rol humano autorizado.");
                if(path.startsWith("/ui/operator/") || (!"GET".equals(request.getMethod()) && (path.endsWith("/reviews") || path.endsWith("/review") || path.endsWith("/qualification-decisions")))) access.requireOperator();
                var match=DRIVER.matcher(path); if(match.matches()) access.driver(UUID.fromString(match.group(1)));
                if(path.startsWith("/ui/") && request.getParameter("driverId")!=null) access.driver(UUID.fromString(request.getParameter("driverId")));
            }
            request.setAttribute("m3.actor",principal.getSubject().orElse("local"));
            chain.doFilter(request,response);
        } catch(ConductorAccesoDenegadoExcepcion ex) { problem(response,403,"Acceso denegado"); }
          catch(ConductorNoEncontradoExcepcion ex) { problem(response,404,"Conductor no encontrado"); }
          catch(IllegalArgumentException ex) { problem(response,400,"Identificador inválido"); }
    }
    private void problem(HttpServletResponse response,int status,String title) throws IOException {
        response.setStatus(status);response.setContentType("application/problem+json");response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"type\":\"about:blank\",\"status\":"+status+",\"title\":\""+title+"\"}");
    }
}
