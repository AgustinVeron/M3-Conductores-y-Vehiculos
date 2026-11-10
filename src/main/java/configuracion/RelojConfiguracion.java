package m3.configuracion;

import m3.aplicacion.puerto.salida.RelojPuerto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

/**
 * Configuración del reloj del sistema para la capa de aplicación.
 */
@Configuration
public class RelojConfiguracion {

    @Bean
    public RelojPuerto clockPort() {
        return Instant::now;
    }
}
