package m3.adaptador.persistencia.entidad;

import m3.dominio.DocumentoEstado;
import m3.dominio.DocumentoTipo;
import m3.dominio.ServicioTipo;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "documents")
public class DocumentoJpaEntidad {

    @Id
    private UUID id;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "vehicle_id")
    private UUID vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentoTipo documentType;

    @Column(name = "document_number", nullable = false, length = 80)
    private String documentNumber;

    @Column(name = "issuing_entity", length = 100)
    private String issuingEntity;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "storage_reference", length = 200)
    private String storageReference;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "document_service_types", joinColumns = @JoinColumn(name = "document_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 10)
    private Set<ServicioTipo> serviceTypes = new HashSet<>();

    @Column(name = "revision", nullable = false)
    private int revision;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private DocumentoEstado status;

    @Column(name = "is_current", nullable = false)
    private boolean current;

    @Column(name = "driver_version")
    private Long driverVersion;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public DocumentoJpaEntidad() {}

    public DocumentoJpaEntidad(UUID id, UUID driverId, UUID vehicleId, DocumentoTipo documentType,
                             String documentNumber, String issuingEntity, Instant issuedAt,
                             Instant validUntil, String storageReference, int revision,
                             DocumentoEstado status, boolean current, Long driverVersion,
                             String rejectionReason, Instant reviewedAt, String reviewedBy,
                             Instant createdAt) {
        this.id = id;
        this.driverId = driverId;
        this.vehicleId = vehicleId;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.issuingEntity = issuingEntity;
        this.issuedAt = issuedAt;
        this.validUntil = validUntil;
        this.storageReference = storageReference;
        this.revision = revision;
        this.status = status;
        this.current = current;
        this.driverVersion = driverVersion;
        this.rejectionReason = rejectionReason;
        this.reviewedAt = reviewedAt;
        this.reviewedBy = reviewedBy;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
    public DocumentoTipo getDocumentType() { return documentType; }
    public void setDocumentType(DocumentoTipo documentType) { this.documentType = documentType; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getIssuingEntity() { return issuingEntity; }
    public void setIssuingEntity(String issuingEntity) { this.issuingEntity = issuingEntity; }
    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }
    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }
    public String getStorageReference() { return storageReference; }
    public void setStorageReference(String storageReference) { this.storageReference = storageReference; }
    public Set<ServicioTipo> getServiceTypes() { return serviceTypes; }
    public void setServiceTypes(Set<ServicioTipo> serviceTypes) { this.serviceTypes = new HashSet<>(serviceTypes); }
    public int getRevision() { return revision; }
    public void setRevision(int revision) { this.revision = revision; }
    public DocumentoEstado getStatus() { return status; }
    public void setStatus(DocumentoEstado status) { this.status = status; }
    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) { this.current = current; }
    public Long getDriverVersion() { return driverVersion; }
    public void setDriverVersion(Long driverVersion) { this.driverVersion = driverVersion; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
