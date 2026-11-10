package m3.adaptador.api.dto;

import m3.aplicacion.casodeuso.ConductorDespachoInformacion;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/** JSON representation of the M5 -> M3 dispatch information contract. */
public record ConductorDespachoInformacionRespuesta(
        @JsonProperty("conductor_id") UUID conductorId,
        @JsonProperty("habilitado") boolean habilitado,
        @JsonProperty("disponible") boolean disponible,
        @JsonProperty("vehiculo") @JsonInclude(JsonInclude.Include.ALWAYS) VehiculoRespuesta vehiculo) {

    public static ConductorDespachoInformacionRespuesta from(ConductorDespachoInformacion dispatchInfo) {
        VehiculoRespuesta vehicle = dispatchInfo.vehiculo() == null
                ? null
                : new VehiculoRespuesta(
                        dispatchInfo.vehiculo().vehiculoId(),
                        dispatchInfo.vehiculo().tipoVehiculo());
        return new ConductorDespachoInformacionRespuesta(
                dispatchInfo.conductorId(),
                dispatchInfo.habilitado(),
                dispatchInfo.disponible(),
                vehicle);
    }

    public record VehiculoRespuesta(
            @JsonProperty("vehiculo_id") UUID vehiculoId,
            @JsonProperty("tipo_vehiculo") String tipoVehiculo) {
    }
}
