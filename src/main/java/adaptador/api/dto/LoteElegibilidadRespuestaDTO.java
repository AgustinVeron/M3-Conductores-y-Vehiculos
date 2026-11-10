package m3.adaptador.api.dto;

import java.util.List;
import java.util.UUID;

public record LoteElegibilidadRespuestaDTO(
    List<DriverEligibilityEntry> results
) {
    public record DriverEligibilityEntry(
        UUID driverId,
        ElegibilidadRespuestaDTO eligibility
    ) {}
}
