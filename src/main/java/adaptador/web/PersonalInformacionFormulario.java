package m3.adaptador.web;

import m3.dominio.Conductor;
import m3.dominio.PersonalInformacion;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/** Form data for completing or updating the driver's personal profile. */
public class PersonalInformacionFormulario {
    @NotBlank(message = "Ingresá tu nombre.")
    @Size(max = 100, message = "El nombre puede tener hasta 100 caracteres.")
    private String firstName;

    @NotBlank(message = "Ingresá tu apellido.")
    @Size(max = 100, message = "El apellido puede tener hasta 100 caracteres.")
    private String lastName;

    @NotBlank(message = "Ingresá tu DNI.")
    @Pattern(regexp = "[0-9]{7,10}", message = "El DNI debe contener entre 7 y 10 dígitos.")
    private String dni;

    @NotNull(message = "Ingresá tu fecha de nacimiento.")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    @NotBlank(message = "Ingresá tu dirección.")
    @Size(max = 255, message = "La dirección puede tener hasta 255 caracteres.")
    private String address;

    @NotBlank(message = "Ingresá tu teléfono.")
    @Size(max = 50, message = "El teléfono puede tener hasta 50 caracteres.")
    private String phone;

    @NotBlank(message = "Ingresá tu correo electrónico.")
    @Email(message = "Ingresá un correo electrónico válido.")
    @Size(max = 150, message = "El correo electrónico puede tener hasta 150 caracteres.")
    private String email;

    @Min(value = 1, message = "Recargá el formulario e intentá nuevamente.")
    @Max(value = Integer.MAX_VALUE, message = "Recargá el formulario e intentá nuevamente.")
    private long version;

    public static PersonalInformacionFormulario from(Conductor driver) {
        PersonalInformacionFormulario form = new PersonalInformacionFormulario();
        PersonalInformacion info = driver.getPersonalInfo();
        form.firstName = info != null ? info.getFirstName() : driver.getDisplayName();
        form.lastName = info != null ? info.getLastName() : "";
        form.dni = info != null ? info.getDni() : "";
        form.birthDate = info != null ? info.getBirthDate() : null;
        form.address = info != null ? info.getAddress() : "";
        form.phone = info != null ? info.getPhone() : "";
        form.email = info != null ? info.getEmail() : "";
        form.version = driver.getVersion();
        return form;
    }

    public PersonalInformacion toPersonalInfo() {
        return new PersonalInformacion(firstName, lastName, dni, birthDate, address, phone, email);
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
