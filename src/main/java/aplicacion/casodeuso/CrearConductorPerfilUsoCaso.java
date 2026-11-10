package m3.aplicacion.casodeuso;

import m3.aplicacion.excepcion.ConductorYaExisteExcepcion;
import m3.aplicacion.puerto.ConductorRepositorio;
import m3.dominio.Conductor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Crear perfil de conductor (HU-01 / CLON-6).
 * Valida que no existan perfiles duplicados para el mismo identity_subject
 * antes de persistir el nuevo conductor.
 */
@Service
@Transactional
public class CrearConductorPerfilUsoCaso {

    private final ConductorRepositorio driverRepository;

    public CrearConductorPerfilUsoCaso(ConductorRepositorio driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Conductor execute(String identitySubject, String name) {
        if (identitySubject == null || identitySubject.trim().isEmpty()) {
            throw new IllegalArgumentException("identitySubject no puede ser nulo ni vacío");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name no puede ser nulo ni vacío");
        }

        String normalizedIdentity = identitySubject.trim();

        if (driverRepository.existsByIdentitySubject(normalizedIdentity)
                || driverRepository.findByIdentitySubject(normalizedIdentity).isPresent()) {
            throw new ConductorYaExisteExcepcion(normalizedIdentity);
        }

        Conductor newDriver = new Conductor(normalizedIdentity, name.trim());
        return driverRepository.save(newDriver);
    }

    public Conductor execute(CreateDriverCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        return execute(command.identitySubject(), command.name());
    }

    public record CreateDriverCommand(String identitySubject, String name) {}
}
