package m3.dominio;

import m3.dominio.excepcion.VersionInconsistenciaExcepcion;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado raíz que representa al Conductor en el dominio de M3.
 * Es una entidad pura sin dependencias de infraestructura ni frameworks.
 * RNF-08: Maneja versión para control de concurrencia optimista / ETag.
 */
public class Conductor {

    private final UUID id;
    private final String identitySubject;
    private String displayName;
    private PersonalInformacion personalInfo;
    private HabilitacionEstado qualificationStatus;
    private String qualificationReason;
    private int version;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Constructor de creación rápida de perfil para HU-01 (RNF-08: inicia en versión 1).
     */
    public Conductor(String identitySubject, String name) {
        this.id = UUID.randomUUID();
        this.identitySubject = validateIdentitySubject(identitySubject);
        this.displayName = validateName(name);
        this.qualificationStatus = HabilitacionEstado.PENDING;
        this.qualificationReason = "Perfil creado pendiente de validación";
        this.version = 1;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Constructor con PersonalInformacion para HU-02.
     */
    public Conductor(UUID id, String identitySubject, PersonalInformacion personalInfo, Instant now) {
        this(
                id,
                identitySubject,
                personalInfo != null ? personalInfo.getFullName() : "Conductor",
                HabilitacionEstado.PENDING,
                1,
                now != null ? now : Instant.now(),
                now != null ? now : Instant.now()
        );
        this.personalInfo = personalInfo;
        this.qualificationReason = "Perfil registrado, pendiente de revisión administrativa";
    }

    /**
     * Constructor completo desde persistencia o seeds de prueba con PersonalInformacion (HU-02).
     */
    public Conductor(UUID id,
                  String identitySubject,
                  PersonalInformacion personalInfo,
                  HabilitacionInformacion qualification,
                  int version,
                  Instant createdAt,
                  Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id es requerido");
        this.identitySubject = validateIdentitySubject(identitySubject);
        this.personalInfo = personalInfo;
        this.displayName = personalInfo != null ? personalInfo.getFullName() : "Conductor";
        this.qualificationStatus = qualification != null && qualification.getStatus() != null
                ? HabilitacionEstado.valueOf(qualification.getStatus().name())
                : HabilitacionEstado.PENDING;
        this.qualificationReason = qualification != null ? qualification.getReason() : null;
        this.version = version > 0 ? version : 1;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt es requerido");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt es requerido");
    }

    /**
     * Constructor estándar para reconstrucción desde persistencia.
     */
    public Conductor(
            UUID id,
            String identitySubject,
            String displayName,
            HabilitacionEstado qualificationStatus,
            long version,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "El id no puede ser nulo");
        this.identitySubject = validateIdentitySubject(identitySubject);
        this.displayName = validateName(displayName);
        this.qualificationStatus = qualificationStatus != null ? qualificationStatus : HabilitacionEstado.PENDING;
        this.version = version > 0 ? (int) version : 1;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public Conductor(UUID id, String identitySubject, String name,
                  HabilitacionEstado qualificationStatus, int version,
                  Instant createdAt, Instant updatedAt) {
        this(id, identitySubject, name, qualificationStatus, (long) version, createdAt, updatedAt);
    }

    private static String validateIdentitySubject(String identitySubject) {
        if (identitySubject == null || identitySubject.trim().isEmpty()) {
            throw new IllegalArgumentException("identity_subject no puede ser nulo ni vacío");
        }
        return identitySubject.trim();
    }

    private static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo ni vacío");
        }
        return name.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getIdentitySubject() {
        return identitySubject;
    }

    public String getDisplayName() {
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }
        if (personalInfo != null) {
            return personalInfo.getFullName();
        }
        return "";
    }

    public String getName() {
        return getDisplayName();
    }

    public HabilitacionEstado getQualificationStatus() {
        return qualificationStatus;
    }

    public ConductorEstado getStatus() {
        return qualificationStatus != null
                ? ConductorEstado.valueOf(qualificationStatus.name())
                : ConductorEstado.PENDING;
    }

    public String getQualificationReason() {
        return qualificationReason;
    }

    public PersonalInformacion getPersonalInfo() {
        return personalInfo;
    }

    public HabilitacionInformacion getQualification() {
        return new HabilitacionInformacion(
                ConductorEstado.valueOf(qualificationStatus.name()),
                qualificationReason != null ? qualificationReason : "Sin observaciones"
        );
    }

    public int getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isOperable() {
        return qualificationStatus == HabilitacionEstado.APPROVED;
    }

    public boolean isOwnedBy(String subject) {
        return this.identitySubject != null && this.identitySubject.equals(subject);
    }

    public void validateVersion(long expectedVersion) {
        if (this.version != expectedVersion) {
            throw new VersionInconsistenciaExcepcion(this.version, expectedVersion);
        }
    }

    public void incrementVersion() {
        this.version++;
        this.updatedAt = Instant.now();
    }

    public void updateName(String newName) {
        this.displayName = validateName(newName);
        this.updatedAt = Instant.now();
    }

    public void updateDisplayName(String newDisplayName) {
        updateName(newDisplayName);
    }

    public void changeQualificationStatus(HabilitacionEstado newStatus) {
        this.qualificationStatus = Objects.requireNonNull(newStatus, "El nuevo estado no puede ser nulo");
        this.updatedAt = Instant.now();
    }

    public void updatePersonalInfo(PersonalInformacion newInfo, Instant now) {
        this.personalInfo = Objects.requireNonNull(newInfo, "La nueva información personal no puede ser nula");
        this.displayName = newInfo.getFullName();
        this.updatedAt = Objects.requireNonNull(now, "updatedAt es requerido");
        this.version++;
    }

    public void updateQualification(HabilitacionInformacion newQualification, Instant now) {
        Objects.requireNonNull(newQualification, "newQualification es requerido");
        this.qualificationStatus = HabilitacionEstado.valueOf(newQualification.getStatus().name());
        this.qualificationReason = newQualification.getReason();
        this.updatedAt = Objects.requireNonNull(now, "updatedAt es requerido");
        this.version++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conductor driver)) return false;
        return Objects.equals(id, driver.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Conductor{" +
                "id=" + id +
                ", identitySubject='" + identitySubject + '\'' +
                ", displayName='" + getDisplayName() + '\'' +
                ", qualificationStatus=" + qualificationStatus +
                ", version=" + version +
                '}';
    }
}
