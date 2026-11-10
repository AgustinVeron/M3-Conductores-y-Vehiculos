package m3.aplicacion.casodeuso;

import m3.aplicacion.excepcion.ConductorAccesoDenegadoExcepcion;
import m3.aplicacion.excepcion.ConductorNoEncontradoExcepcion;
import m3.aplicacion.puerto.entrada.ObtenerConductorPerfilUsoCaso;
import m3.aplicacion.puerto.entrada.ObtenerMiConductorPerfilUsoCaso;
import m3.aplicacion.puerto.salida.ActualPrincipalPuerto;
import m3.aplicacion.puerto.salida.ConductorRepositorioPuerto;
import m3.dominio.Conductor;

import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Aplicación que implementa los casos de uso de consulta de perfil de conductor (HU-02).
 * Aplica rigurosamente las reglas de seguridad sobre objetos:
 * - El conductor solo puede ver su propio perfil.
 * - Los operadores administrativos pueden consultar cualquier perfil.
 */
public class ObtenerConductorPerfilServicio implements ObtenerConductorPerfilUsoCaso, ObtenerMiConductorPerfilUsoCaso {

    private final ConductorRepositorioPuerto driverRepository;
    private final ActualPrincipalPuerto currentPrincipal;

    public ObtenerConductorPerfilServicio(ConductorRepositorioPuerto driverRepository,
                                  ActualPrincipalPuerto currentPrincipal) {
        this.driverRepository = Objects.requireNonNull(driverRepository, "driverRepository es requerido");
        this.currentPrincipal = Objects.requireNonNull(currentPrincipal, "currentPrincipal es requerido");
    }

    @Override
    public Conductor getMyDriverProfile() {
        if (!currentPrincipal.isAuthenticated()) {
            throw new ConductorAccesoDenegadoExcepcion("Operación no autorizada: sesión o token no autenticado.");
        }

        String subject = currentPrincipal.getSubject()
                .orElseThrow(() -> new ConductorAccesoDenegadoExcepcion("Identidad de usuario (subject) no disponible en el contexto de seguridad."));

        return driverRepository.findByIdentitySubject(subject)
                .orElseThrow(() -> ConductorNoEncontradoExcepcion.forSubject(subject));
    }

    @Override
    public Conductor getDriverProfileById(UUID driverId) {
        Objects.requireNonNull(driverId, "El driverId no puede ser nulo");

        Conductor driver = driverRepository.findById(driverId)
                .orElseThrow(() -> ConductorNoEncontradoExcepcion.forId(driverId));

        if (!hasAccessToDriver(driver)) {
            throw new ConductorAccesoDenegadoExcepcion("Acceso denegado: no posee permisos para consultar la información de este conductor.");
        }

        return driver;
    }

    private boolean hasAccessToDriver(Conductor driver) {
        // 1. Si es operador o administrador del sistema, tiene acceso irrestricto de consulta
        if (currentPrincipal.hasRole("ROLE_OPERATOR") || currentPrincipal.hasRole("OPERATOR")
                || currentPrincipal.hasRole("ROLE_ADMIN") || currentPrincipal.hasRole("ADMIN")) {
            return true;
        }

        // 2. Si es el propietario del perfil (mismo subject de M1)
        return currentPrincipal.getSubject()
                .map(driver::isOwnedBy)
                .orElse(false);
    }
}
