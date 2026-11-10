package m3.adaptador.seguridad;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
@Component
public class DesarrolloModo {
    private final boolean enabled;
    public DesarrolloModo(Environment environment) {
        enabled=environment.acceptsProfiles(Profiles.of("local","demo","test")) && !environment.acceptsProfiles(Profiles.of("production","docker"));
    }
    public boolean enabled() { return enabled; }
}
