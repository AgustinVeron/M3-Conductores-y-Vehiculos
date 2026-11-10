package m3.dominio;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object inmutable que representa los datos personales del Conductor (Persona)
 * según el modelado de dominio de M3.
 */
public final class PersonalInformacion {
    private final String firstName;
    private final String lastName;
    private final String dni;
    private final LocalDate birthDate;
    private final String address;
    private final String phone;
    private final String email;

    public PersonalInformacion(String firstName,
                        String lastName,
                        String dni,
                        LocalDate birthDate,
                        String address,
                        String phone,
                        String email) {
        this(firstName,lastName,dni,birthDate,address,phone,email,false);
    }

    /** Reconstitutes an existing, possibly incomplete profile without inventing missing data. */
    public static PersonalInformacion fromStored(String firstName,String lastName,String dni,LocalDate birthDate,String address,String phone,String email) {
        return new PersonalInformacion(firstName,lastName,dni,birthDate,address,phone,email,true);
    }

    private PersonalInformacion(String firstName,String lastName,String dni,LocalDate birthDate,String address,String phone,String email,boolean stored) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo ni estar vacío");
        }
        if (!stored && (lastName == null || lastName.isBlank())) {
            throw new IllegalArgumentException("El apellido no puede ser nulo ni estar vacío");
        }
        if (!stored && (dni == null || dni.isBlank())) {
            throw new IllegalArgumentException("El DNI no puede ser nulo ni estar vacío");
        }
        if (!stored && (email == null || email.isBlank() || !email.contains("@"))) {
            throw new IllegalArgumentException("El correo electrónico debe ser válido");
        }
        this.firstName = firstName.trim();
        this.lastName = lastName == null ? "" : lastName.trim();
        this.dni = dni == null || dni.isBlank() ? null : dni.trim();
        this.birthDate = birthDate;
        this.address = address != null ? address.trim() : null;
        this.phone = phone != null ? phone.trim() : null;
        this.email = email == null || email.isBlank() ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }

    public String getDni() {
        return dni;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonalInformacion that = (PersonalInformacion) o;
        return Objects.equals(firstName, that.firstName) &&
               Objects.equals(lastName, that.lastName) &&
               Objects.equals(dni, that.dni) &&
               Objects.equals(birthDate, that.birthDate) &&
               Objects.equals(address, that.address) &&
               Objects.equals(phone, that.phone) &&
               Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, dni, birthDate, address, phone, email);
    }

    @Override
    public String toString() {
        return "PersonalInformacion{" +
                "fullName='" + getFullName() + '\'' +
                ", dni='" + dni + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
