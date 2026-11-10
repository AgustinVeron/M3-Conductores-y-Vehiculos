package m3.configuracion;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "m3.integration")
public record IntegracionPropiedades(
    String m4BaseUrl,
    String m6BaseUrl,
    int timeoutMs
) {}
