package m3.adaptador.api;

import m3.dominio.PersonalInformacion;
import java.time.LocalDate;

/**
 * DTO de salida con los datos personales del conductor.
 */
public record PersonalInformacionRespuesta(
        String firstName,
        String lastName,
        String fullName,
        String dni,
        LocalDate birthDate,
        String address,
        String phone,
        String email
) {
    public static PersonalInformacionRespuesta from(PersonalInformacion info) {
        if (info == null) return null;
        return new PersonalInformacionRespuesta(
                info.getFirstName(),
                info.getLastName(),
                info.getFullName(),
                info.getDni(),
                info.getBirthDate(),
                info.getAddress(),
                info.getPhone(),
                info.getEmail()
        );
    }
}
