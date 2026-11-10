package m3.adaptador.integracion;
import m3.adaptador.seguridad.DesarrolloModo;
import m3.dominio.excepcion.IntegracionNoDisponibleExcepcion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import java.time.*;
import java.util.*;
@Component
public class ServicioTokenProveedor {
    private final RestClient client; private final DesarrolloModo mode;
    private final String uri,id,secret; private final Map<String,Cached> cache=new HashMap<>();
    private record Cached(String token,Instant until) {}
    public ServicioTokenProveedor(DesarrolloModo mode,@Value("${m3.integration.token-uri:}") String uri,
            @Value("${m3.integration.client-id:}") String id,@Value("${m3.integration.client-secret:}") String secret) {
        this.mode=mode; this.uri=uri; this.id=id; this.secret=secret;
        var factory=new JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5)); client=RestClient.builder().requestFactory(factory).build();
    }
    public synchronized String token(String scope) {
        if(uri.isBlank() && mode.enabled()) return null;
        if(uri.isBlank() || id.isBlank() || secret.isBlank()) throw new IntegracionNoDisponibleExcepcion("Faltan credenciales técnicas para la integración.");
        Cached existing=cache.get(scope); if(existing!=null && existing.until().isAfter(Instant.now())) return existing.token();
        try {
            var form=new LinkedMultiValueMap<String,String>();form.add("grant_type","client_credentials");form.add("scope",scope);
            var body=client.post().uri(uri).headers(h->h.setBasicAuth(id,secret)).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(TokenResponse.class);
            if(body==null || body.access_token()==null || body.access_token().isBlank() || body.expires_in()<=0) throw new IllegalStateException();
            cache.put(scope,new Cached(body.access_token(),Instant.now().plusSeconds(Math.max(0,body.expires_in()-30))));return body.access_token();
        } catch(RuntimeException ex) { throw new IntegracionNoDisponibleExcepcion("No se pudo obtener la identidad técnica de M3."); }
    }
    public record TokenResponse(String access_token,long expires_in) {}
}
