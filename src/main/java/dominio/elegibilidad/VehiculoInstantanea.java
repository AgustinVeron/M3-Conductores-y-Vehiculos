package m3.dominio.elegibilidad;

import m3.dominio.HabilitacionEstado;
import m3.dominio.ServicioTipo;

import java.util.Set;
import java.util.UUID;

public record VehiculoInstantanea(
        UUID id,
        HabilitacionEstado qualificationStatus,
        Set<ServicioTipo> serviceTypes,
        DocumentoInstantanea insurance, DocumentoInstantanea registration
) {
    public boolean supportsService(ServicioTipo serviceType) {
        return serviceTypes != null && serviceTypes.contains(serviceType);
    }
}
