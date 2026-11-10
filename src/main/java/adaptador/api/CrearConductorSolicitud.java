package m3.adaptador.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de creación de perfil de conductor.
 * Incluye validaciones Jakarta Bean Validation y soporte para convenciones
 * camelCase y snake_case (identity_subject, full_name).
 */
public class CrearConductorSolicitud {

    @NotBlank(message = "El identity_subject es obligatorio")
    @Size(max = 255, message = "El identity_subject no puede superar los 255 caracteres")
    @JsonProperty("identitySubject")
    @JsonAlias({"identity_subject", "subject"})
    private String identitySubject;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
    @JsonProperty("name")
    @JsonAlias({"fullName", "full_name", "nombre"})
    private String name;

    public CrearConductorSolicitud() {
    }

    public CrearConductorSolicitud(String identitySubject, String name) {
        this.identitySubject = identitySubject;
        this.name = name;
    }

    public String getIdentitySubject() {
        return identitySubject;
    }

    public void setIdentitySubject(String identitySubject) {
        this.identitySubject = identitySubject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
