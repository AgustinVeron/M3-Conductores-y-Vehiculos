package m3.adaptador.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record LoteElegibilidadSolicitudDTO(
    @NotEmpty @Size(min=1, max=100) List<UUID> driverIds
) {}
