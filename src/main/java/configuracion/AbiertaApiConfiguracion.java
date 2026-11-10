package m3.configuracion;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AbiertaApiConfiguracion {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("M3 — Conductores y Vehículos")
                        .version("0.2.0")
                        .description("Microservicio M3 de Conductores y Vehículos del TPI Plataforma distribuida de movilidad urbana."))
                .servers(List.of(
                        new Server().url("http://localhost:3003").description("Local Development Server")
                ));
    }
}
