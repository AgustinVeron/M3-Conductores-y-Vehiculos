package m3.configuracion;

import m3.aplicacion.puerto.salida.RelojPuerto;
import m3.aplicacion.puerto.salida.ConductorRepositorioPuerto;
import m3.dominio.Conductor;
import m3.dominio.PersonalInformacion;
import m3.dominio.HabilitacionInformacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Inicializador de datos sintéticos (seeds) para desarrollo y demostración local.
 * Carga perfiles con los distintos estados de habilitación previstos en el negocio (HU-02).
 */
@Component
@Profile("demo")
public class DatosInicializador implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosInicializador.class);

    private final ConductorRepositorioPuerto driverRepository;
    private final RelojPuerto clockPort;

    public DatosInicializador(ConductorRepositorioPuerto driverRepository, RelojPuerto clockPort) {
        this.driverRepository = driverRepository;
        this.clockPort = clockPort;
    }

    @Override
    public void run(String... args) {
        Instant now = clockPort.now();

        // 1. Conductor principal para la sesión local por defecto (Habilitado / APPROVED)
        String defaultSub = "user-sub-123";
        if (!driverRepository.existsByIdentitySubject(defaultSub)) {
            UUID id1 = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
            PersonalInformacion info1 = new PersonalInformacion(
                    "Lautaro",
                    "González",
                    "42158963",
                    LocalDate.of(1999, 8, 20),
                    "Av. Castelli 950, Resistencia, Chaco",
                    "+54 362 4558899",
                    "lautaro.gonzalez@example.com"
            );
            Conductor driver1 = new Conductor(
                    id1,
                    defaultSub,
                    info1,
                    HabilitacionInformacion.approved(),
                    1,
                    now.minusSeconds(86400 * 5),
                    now.minusSeconds(86400 * 2)
            );
            driverRepository.save(driver1);
            log.info("Semilla cargada: Conductor APPROVED -> ID: {}, Identity: {}", id1, defaultSub);
        }

        // 2. Conductor con estado PENDING (Pendiente de revisión)
        String pendingSub = "driver-pending-456";
        if (!driverRepository.existsByIdentitySubject(pendingSub)) {
            UUID id2 = UUID.fromString("b1ffcd88-8b1a-4fe7-aa5c-5aa8ac270b22");
            PersonalInformacion info2 = new PersonalInformacion(
                    "Carlos",
                    "Benítez",
                    "38741258",
                    LocalDate.of(1994, 3, 12),
                    "Calle Güemes 340, Corrientes",
                    "+54 379 4112233",
                    "carlos.benitez@example.com"
            );
            Conductor driver2 = new Conductor(
                    id2,
                    pendingSub,
                    info2,
                    HabilitacionInformacion.pending(),
                    1,
                    now.minusSeconds(86400 * 2),
                    now.minusSeconds(86400 * 2)
            );
            driverRepository.save(driver2);
            log.info("Semilla cargada: Conductor PENDING -> ID: {}", id2);
        }

        // 3. Conductor con estado OBSERVED (Con observaciones específicas)
        String observedSub = "driver-observed-789";
        if (!driverRepository.existsByIdentitySubject(observedSub)) {
            UUID id3 = UUID.fromString("c2eebe77-7a2b-4cd6-994b-4bb7bd160c33");
            PersonalInformacion info3 = new PersonalInformacion(
                    "Mariana",
                    "López",
                    "36985214",
                    LocalDate.of(1992, 11, 5),
                    "Av. 9 de Julio 1450, Resistencia, Chaco",
                    "+54 362 4998877",
                    "mariana.lopez@example.com"
            );
            Conductor driver3 = new Conductor(
                    id3,
                    observedSub,
                    info3,
                    HabilitacionInformacion.observed("La fotografía de la licencia de conducir no es legible. Por favor presentar una imagen nítida."),
                    2,
                    now.minusSeconds(86400 * 10),
                    now.minusSeconds(3600 * 4)
            );
            driverRepository.save(driver3);
            log.info("Semilla cargada: Conductor OBSERVED -> ID: {}", id3);
        }

        // 4. Conductor con estado SUSPENDED (Suspendido)
        String suspendedSub = "driver-suspended-000";
        if (!driverRepository.existsByIdentitySubject(suspendedSub)) {
            UUID id4 = UUID.fromString("d3ffbf66-6f3c-4bc5-883a-3aa6ac050d44");
            PersonalInformacion info4 = new PersonalInformacion(
                    "Esteban",
                    "Duarte",
                    "33112233",
                    LocalDate.of(1988, 7, 19),
                    "Av. San Martín 820, Fontana, Chaco",
                    "+54 362 4123456",
                    "esteban.duarte@example.com"
            );
            Conductor driver4 = new Conductor(
                    id4,
                    suspendedSub,
                    info4,
                    HabilitacionInformacion.suspended("Suspensión preventiva por acumulación de reclamos de seguridad."),
                    3,
                    now.minusSeconds(86400 * 30),
                    now.minusSeconds(3600 * 12)
            );
            driverRepository.save(driver4);
            log.info("Semilla cargada: Conductor SUSPENDED -> ID: {}", id4);
        }
    }
}
