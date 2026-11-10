package m3.dominio.elegibilidad;

import m3.dominio.HabilitacionEstado;

import java.util.UUID;

public record ConductorInstantanea(
        UUID id,
        String identitySubject,
        String displayName,
        HabilitacionEstado qualificationStatus,
        VehiculoInstantanea selectedVehicle,
        DocumentoInstantanea license,
        int version
) {
}
