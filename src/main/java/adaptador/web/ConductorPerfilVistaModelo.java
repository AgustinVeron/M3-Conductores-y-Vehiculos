package m3.adaptador.web;

import m3.dominio.Conductor;
import m3.dominio.ConductorEstado;

import java.time.format.DateTimeFormatter;

/**
 * Modelo de vista (ViewModel) para la renderización de la pantalla de perfil del conductor en Thymeleaf.
 * Facilita la presentación de badges de estado, mensajes de orientación y datos personales.
 */
public record ConductorPerfilVistaModelo(
        String id,
        String identitySubject,
        String firstName,
        String lastName,
        String fullName,
        String dni,
        String birthDateFormatted,
        String address,
        String phone,
        String email,
        ConductorEstado status,
        String statusDescription,
        String qualificationReason,
        boolean operable,
        int version,
        String createdAtFormatted,
        String updatedAtFormatted,
        String badgeCssClass,
        String alertCssClass,
        String guidanceMessage
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static ConductorPerfilVistaModelo from(Conductor driver) {
        if (driver == null) {
            return null;
        }

        String birthDateStr = driver.getPersonalInfo().getBirthDate() != null
                ? driver.getPersonalInfo().getBirthDate().format(DATE_FORMATTER)
                : "No especificada";

        String createdStr = driver.getCreatedAt() != null
                ? DATE_TIME_FORMATTER.format(driver.getCreatedAt().atZone(java.time.ZoneId.systemDefault()))
                : "-";

        String updatedStr = driver.getUpdatedAt() != null
                ? DATE_TIME_FORMATTER.format(driver.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()))
                : "-";

        String badgeClass = switch (driver.getStatus()) {
            case APPROVED -> "bg-success";
            case PENDING -> "bg-warning text-dark";
            case OBSERVED -> "bg-info text-dark";
            case SUSPENDED -> "bg-danger";
            case REJECTED -> "bg-secondary";
        };

        String alertClass = switch (driver.getStatus()) {
            case APPROVED -> "alert-success";
            case PENDING -> "alert-warning";
            case OBSERVED -> "alert-info";
            case SUSPENDED -> "alert-danger";
            case REJECTED -> "alert-secondary";
        };

        String guidance = switch (driver.getStatus()) {
            case APPROVED -> "Tu perfil está aprobado. Consultá Habilitación para verificar también el vehículo y la documentación.";
            case PENDING -> "Tu solicitud de habilitación se encuentra en revisión administrativa por nuestro equipo de operadores.";
            case OBSERVED -> "Tu perfil o documentación presenta observaciones que debes subsanar para poder continuar con la habilitación.";
            case SUSPENDED -> "Tu perfil se encuentra temporalmente suspendido por razones administrativas. Comunícate con soporte.";
            case REJECTED -> "Tu solicitud de habilitación ha sido rechazada por el operador.";
        };

        return new ConductorPerfilVistaModelo(
                driver.getId().toString(),
                driver.getIdentitySubject(),
                driver.getPersonalInfo().getFirstName(),
                driver.getPersonalInfo().getLastName(),
                driver.getPersonalInfo().getFullName(),
                driver.getPersonalInfo().getDni(),
                birthDateStr,
                driver.getPersonalInfo().getAddress() != null ? driver.getPersonalInfo().getAddress() : "No registrado",
                driver.getPersonalInfo().getPhone() != null ? driver.getPersonalInfo().getPhone() : "No registrado",
                driver.getPersonalInfo().getEmail(),
                driver.getStatus(),
                driver.getStatus().getDescription(),
                driver.getQualificationReason(),
                driver.isOperable(),
                driver.getVersion(),
                createdStr,
                updatedStr,
                badgeClass,
                alertClass,
                guidance
        );
    }

    // Métodos accesores para compatibilidad con convenciones JavaBean en pruebas y plantillas
    public String getFullName() { return fullName; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDni() { return dni; }
    public String getEmail() { return email; }
    public String getStatusDescription() { return statusDescription; }
}
