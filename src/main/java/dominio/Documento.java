package m3.dominio;

import m3.dominio.excepcion.DominioValidacionExcepcion;
import m3.dominio.excepcion.InvalidoDocumentoPeriodoExcepcion;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Documento {
    private final UUID id;
    private final UUID driverId;
    private final UUID vehicleId;
    private final DocumentoTipo documentType;
    private final String documentNumber;
    private final String issuingEntity;
    private final Instant issuedAt;
    private final Instant validUntil;
    private final String storageReference;
    private final Set<ServicioTipo> serviceTypes;
    private final int revision;
    private DocumentoEstado status;
    private boolean isCurrent;
    private Long driverVersion;
    private String rejectionReason;
    private Instant reviewedAt;
    private String reviewedBy;
    private final Instant createdAt;

    public Documento(UUID id, UUID driverId, UUID vehicleId, DocumentoTipo documentType,
                    String documentNumber, String issuingEntity, Instant issuedAt,
                    Instant validUntil, String storageReference, int revision,
                    DocumentoEstado status, boolean isCurrent, Long driverVersion,
                    String rejectionReason, Instant reviewedAt, String reviewedBy,
                    Instant createdAt) {
        this(id, driverId, vehicleId, documentType, documentNumber, issuingEntity, issuedAt,
                validUntil, storageReference, revision, status, isCurrent, driverVersion,
                rejectionReason, reviewedAt, reviewedBy, createdAt, Set.of());
    }

    public Documento(UUID id, UUID driverId, UUID vehicleId, DocumentoTipo documentType,
                    String documentNumber, String issuingEntity, Instant issuedAt,
                    Instant validUntil, String storageReference, int revision,
                    DocumentoEstado status, boolean isCurrent, Long driverVersion,
                    String rejectionReason, Instant reviewedAt, String reviewedBy,
                    Instant createdAt, Set<ServicioTipo> serviceTypes) {
        this.id = Objects.requireNonNull(id, "document id cannot be null");
        this.driverId = Objects.requireNonNull(driverId, "driver id cannot be null");
        this.vehicleId = vehicleId;
        this.documentType = Objects.requireNonNull(documentType, "documentType cannot be null");
        this.documentNumber = Objects.requireNonNull(documentNumber, "documentNumber cannot be null");
        this.issuingEntity = issuingEntity;
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt cannot be null");
        this.validUntil = Objects.requireNonNull(validUntil, "validUntil cannot be null");
        this.storageReference = storageReference;
        this.serviceTypes = Set.copyOf(Objects.requireNonNull(serviceTypes, "serviceTypes cannot be null"));
        this.revision = Math.max(revision, 1);
        this.status = status != null ? status : DocumentoEstado.PENDING;
        this.isCurrent = isCurrent;
        this.driverVersion = driverVersion != null && driverVersion > 0 ? driverVersion : 1L;
        this.rejectionReason = rejectionReason;
        this.reviewedAt = reviewedAt;
        this.reviewedBy = reviewedBy;
        this.createdAt = createdAt != null ? createdAt : Instant.now();

        if (!issuedAt.isBefore(validUntil)) {
            throw new InvalidoDocumentoPeriodoExcepcion(issuedAt, validUntil);
        }
    }

    public static Documento createPendingRevision(UUID driverId, UUID vehicleId,
                                                 DocumentoTipo documentType, String documentNumber,
                                                 String issuingEntity, Instant issuedAt,
                                                 Instant validUntil, String storageReference,
                                                 int revision) {
        return new Documento(
                UUID.randomUUID(), driverId, vehicleId, documentType, documentNumber,
                issuingEntity, issuedAt, validUntil, storageReference, revision,
                DocumentoEstado.PENDING, true, 1L, null, null, null, Instant.now()
        );
    }

    public static Documento createPendingLicense(UUID driverId, String documentNumber,
                                                Instant issuedAt, Instant validUntil,
                                                Set<ServicioTipo> serviceTypes,
                                                String storageReference, int revision,
                                                long driverVersion) {
        if (serviceTypes == null || serviceTypes.isEmpty()) {
            throw new DominioValidacionExcepcion("License must allow at least one service type");
        }
        if (documentNumber == null || documentNumber.isBlank() || documentNumber.length() > 80) {
            throw new DominioValidacionExcepcion("License number must be between 1 and 80 characters");
        }
        return new Documento(UUID.randomUUID(), driverId, null, DocumentoTipo.LICENSE,
                documentNumber.trim(), null, issuedAt, validUntil, storageReference, revision,
                DocumentoEstado.PENDING, true, driverVersion, null, null, null, Instant.now(), serviceTypes);
    }

    public void review(RevisionDecision decision, String reason, String actor, long newDriverVersion) {
        String normalizedReason = validateReview(decision, reason);
        this.status = switch (decision) {
            case APPROVED -> DocumentoEstado.APPROVED;
            case OBSERVED -> DocumentoEstado.OBSERVED;
            case REJECTED -> DocumentoEstado.REJECTED;
        };
        this.rejectionReason = normalizedReason;
        this.reviewedBy = actor != null && !actor.isBlank() ? actor : "OPERATOR";
        this.reviewedAt = Instant.now();
        this.driverVersion = newDriverVersion;
    }

    /** Validates a review before the enclosing use case changes the driver's version. */
    public String validateReview(RevisionDecision decision, String reason) {
        Objects.requireNonNull(decision, "Review decision must not be null");
        String normalizedReason = reason != null && !reason.isBlank() ? reason.trim() : null;
        if (normalizedReason != null && normalizedReason.length() > 500) {
            throw new DominioValidacionExcepcion("Reason must not exceed 500 characters");
        }
        if (decision.requiresReason() && normalizedReason == null) {
            throw new DominioValidacionExcepcion(decision.missingReasonMessage());
        }
        return normalizedReason;
    }

    public void markAsNonCurrent() { this.isCurrent = false; }
    public boolean isExpired(Instant currentInstant) { return !currentInstant.isBefore(this.validUntil); }

    public UUID getId() { return id; }
    public UUID getDriverId() { return driverId; }
    public UUID getVehicleId() { return vehicleId; }
    public DocumentoTipo getDocumentType() { return documentType; }
    public String getDocumentNumber() { return documentNumber; }
    public String getIssuingEntity() { return issuingEntity; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getValidUntil() { return validUntil; }
    public String getStorageReference() { return storageReference; }
    public Set<ServicioTipo> getServiceTypes() { return serviceTypes; }
    public int getRevision() { return revision; }
    public DocumentoEstado getStatus() { return status; }
    public boolean isCurrent() { return isCurrent; }
    public void assignDriverVersion(long version) { if(version<1) throw new IllegalArgumentException("Invalid driver version"); this.driverVersion=version; }

    public Long getDriverVersion() { return driverVersion; }
    public String getRejectionReason() { return rejectionReason; }
    public Instant getReviewedAt() { return reviewedAt; }
    public String getReviewedBy() { return reviewedBy; }
    public Instant getCreatedAt() { return createdAt; }
}
