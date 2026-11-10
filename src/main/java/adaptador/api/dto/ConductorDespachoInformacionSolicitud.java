package m3.adaptador.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Request body of the M5 dispatch information query. */
public record ConductorDespachoInformacionSolicitud(
        @JsonProperty("conductor_id") @NotNull UUID conductorId) {
}
