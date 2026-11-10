package m3.adaptador.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonAlias;

public record ViajeCompletadaUbicacionSolicitud(
        @JsonAlias("id_viaje") @NotBlank @Size(max = 128) String tripId,
        @JsonAlias("id_conductor") @NotNull UUID driverId,
        @JsonAlias("estado") @NotBlank String status,
        @Valid @NotNull Destination destination,
        @JsonAlias("puntuacion_conductor") @Min(1) @Max(5) Integer driverRating,
        @JsonAlias("fecha_finalizacion") OffsetDateTime completedAt) {

    public record Destination(
            @JsonAlias("latitud") @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
            @JsonAlias("longitud") @DecimalMin("-180.0") @DecimalMax("180.0") double longitude) {
    }
}
