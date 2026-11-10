package m3.adaptador.api.dto;

import m3.dominio.elegibilidad.ElegibilidadResultado;
import java.time.Instant;
import java.util.List;

public record ElegibilidadRespuestaDTO(
    boolean eligible,
    List<String> reasons,
    Instant checkedAt,
    int driverVersion,
    Instant validUntil
) {
    public static ElegibilidadRespuestaDTO from(ElegibilidadResultado result) {
        return new ElegibilidadRespuestaDTO(
            result.eligible(),
            result.reasons(),
            result.checkedAt(),
            result.driverVersion(),
            result.validUntil()
        );
    }
}
