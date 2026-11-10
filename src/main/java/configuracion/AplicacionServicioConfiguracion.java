package m3.configuracion;

import m3.aplicacion.puerto.entrada.VerificarConductorAptitudUsoCaso;
import m3.aplicacion.puerto.salida.RelojPuerto;
import m3.aplicacion.puerto.salida.ActualPrincipalPuerto;
import m3.aplicacion.puerto.salida.ConductorElegibilidadPuerto;
import m3.aplicacion.puerto.salida.ConductorRepositorioPuerto;
import m3.aplicacion.casodeuso.VerificarConductorAptitudServicio;
import m3.aplicacion.casodeuso.ObtenerConductorPerfilServicio;
import m3.dominio.elegibilidad.ElegibilidadVerificador;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de inyección de dependencias para los casos de uso de Aplicación.
 * Mantiene la capa de aplicación desacoplada de Spring sin anotaciones @Service en los casos de uso.
 */
@Configuration
public class AplicacionServicioConfiguracion {

    @Bean
    public ObtenerConductorPerfilServicio getDriverProfileService(ConductorRepositorioPuerto driverRepositoryPort,
                                                           ActualPrincipalPuerto currentPrincipalPort) {
        return new ObtenerConductorPerfilServicio(driverRepositoryPort, currentPrincipalPort);
    }

    @Bean
    public ElegibilidadVerificador eligibilityChecker() {
        return new ElegibilidadVerificador();
    }

    @Bean
    public VerificarConductorAptitudUsoCaso checkDriverAptitudeUseCase(
            ConductorElegibilidadPuerto driverEligibilityPort,
            ElegibilidadVerificador eligibilityChecker,
            RelojPuerto clockPort,
            ActualPrincipalPuerto currentPrincipalPort) {
        return new VerificarConductorAptitudServicio(driverEligibilityPort, eligibilityChecker, clockPort, currentPrincipalPort);
    }
}
