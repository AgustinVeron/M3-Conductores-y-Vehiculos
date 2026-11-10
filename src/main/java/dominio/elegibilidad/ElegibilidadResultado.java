package m3.dominio.elegibilidad;

import java.time.Instant;
import java.util.List;

public record ElegibilidadResultado(
        boolean eligible,
        List<String> reasons,
        Instant checkedAt,
        int driverVersion,
        Instant validUntil
) {
}
