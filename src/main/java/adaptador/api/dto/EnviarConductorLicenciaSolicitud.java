package m3.adaptador.api.dto;

import m3.dominio.ServicioTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

public record EnviarConductorLicenciaSolicitud(
        @NotBlank @Size(max = 80) String documentNumber,
        @NotNull Instant issuedAt,
        @NotNull Instant validUntil,
        @NotEmpty Set<@NotNull ServicioTipo> serviceTypes,
        @Size(max = 200) String storageReference
) {}
