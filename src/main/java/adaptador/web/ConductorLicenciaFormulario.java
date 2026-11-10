package m3.adaptador.web;

import m3.dominio.ServicioTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

public class ConductorLicenciaFormulario {
    @NotBlank @Size(max = 80)
    private String documentNumber;
    @NotNull
    private LocalDate issuedAt;
    @NotNull
    private LocalDate validUntil;
    @NotEmpty
    private Set<ServicioTipo> serviceTypes;
    private long expectedVersion;

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public LocalDate getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDate issuedAt) { this.issuedAt = issuedAt; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
    public Set<ServicioTipo> getServiceTypes() { return serviceTypes; }
    public void setServiceTypes(Set<ServicioTipo> serviceTypes) { this.serviceTypes = serviceTypes; }
    public long getExpectedVersion() { return expectedVersion; }
    public void setExpectedVersion(long expectedVersion) { this.expectedVersion = expectedVersion; }
}
