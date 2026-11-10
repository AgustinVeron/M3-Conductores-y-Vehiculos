package m3.aplicacion.casodeuso;

import m3.aplicacion.excepcion.ConductorAccesoDenegadoExcepcion;
import m3.aplicacion.excepcion.ConductorNoEncontradoExcepcion;
import m3.aplicacion.puerto.entrada.VerificarConductorAptitudUsoCaso;
import m3.aplicacion.puerto.salida.RelojPuerto;
import m3.aplicacion.puerto.salida.ActualPrincipalPuerto;
import m3.aplicacion.puerto.salida.ConductorElegibilidadPuerto;
import m3.dominio.elegibilidad.ConductorInstantanea;
import m3.dominio.elegibilidad.ElegibilidadVerificador;
import m3.dominio.elegibilidad.ElegibilidadResultado;

import java.util.Objects;
import java.util.UUID;

public class VerificarConductorAptitudServicio implements VerificarConductorAptitudUsoCaso {

    private final ConductorElegibilidadPuerto driverEligibilityPort;
    private final ElegibilidadVerificador eligibilityChecker;
    private final RelojPuerto clockPort;
    private final ActualPrincipalPuerto currentPrincipal;

    public VerificarConductorAptitudServicio(ConductorElegibilidadPuerto driverEligibilityPort,
                                      ElegibilidadVerificador eligibilityChecker,
                                      RelojPuerto clockPort,
                                      ActualPrincipalPuerto currentPrincipal) {
        this.driverEligibilityPort = Objects.requireNonNull(driverEligibilityPort, "driverEligibilityPort es requerido");
        this.eligibilityChecker = Objects.requireNonNull(eligibilityChecker, "eligibilityChecker es requerido");
        this.clockPort = Objects.requireNonNull(clockPort, "clockPort es requerido");
        this.currentPrincipal = Objects.requireNonNull(currentPrincipal, "currentPrincipal es requerido");
    }

    @Override
    public ElegibilidadResultado execute(UUID driverId) {
        Objects.requireNonNull(driverId, "driverId no puede ser nulo");
        ConductorInstantanea snapshot = driverEligibilityPort.findSnapshotById(driverId)
                .orElseThrow(() -> ConductorNoEncontradoExcepcion.forId(driverId));
        checkAuthorization(snapshot.identitySubject());
        return eligibilityChecker.check(snapshot, clockPort.now());
    }

    @Override
    public ElegibilidadResultado executeForSelf() {
        if (!currentPrincipal.isAuthenticated()) {
            throw new ConductorAccesoDenegadoExcepcion("Operación no autorizada: usuario no autenticado.");
        }
        String subject = currentPrincipal.getSubject()
                .orElseThrow(() -> new ConductorAccesoDenegadoExcepcion("Identidad de usuario no disponible en el contexto."));
        ConductorInstantanea snapshot = driverEligibilityPort.findSnapshotByIdentitySubject(subject)
                .orElseThrow(() -> ConductorNoEncontradoExcepcion.forSubject(subject));
        return eligibilityChecker.check(snapshot, clockPort.now());
    }

    @Override
    public ElegibilidadResultado executeBySubject(String identitySubject) {
        Objects.requireNonNull(identitySubject, "identitySubject no puede ser nulo");
        ConductorInstantanea snapshot = driverEligibilityPort.findSnapshotByIdentitySubject(identitySubject)
                .orElseThrow(() -> ConductorNoEncontradoExcepcion.forSubject(identitySubject));
        checkAuthorization(snapshot.identitySubject());
        return eligibilityChecker.check(snapshot, clockPort.now());
    }

    private void checkAuthorization(String targetSubject) {
        if (currentPrincipal.hasRole("SCOPE_m3:eligibility:read") || currentPrincipal.hasRole("SCOPE_m3:drivers:read") || currentPrincipal.hasRole("ROLE_OPERATOR") || currentPrincipal.hasRole("OPERATOR")
                || currentPrincipal.hasRole("ROLE_OPERADOR") || currentPrincipal.hasRole("OPERADOR")
                || currentPrincipal.hasRole("ROLE_ADMIN") || currentPrincipal.hasRole("ADMIN")) {
            return;
        }
        if (currentPrincipal.getSubject().filter(s -> s.equalsIgnoreCase(targetSubject)).isPresent()) {
            return;
        }
        throw new ConductorAccesoDenegadoExcepcion("No tiene permisos para consultar la aptitud de este conductor.");
    }
}
