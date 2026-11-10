package m3.adaptador.persistencia.mapeador;

import m3.adaptador.persistencia.entidad.VehiculoJpaEntidad;
import m3.dominio.Vehiculo;
import org.springframework.stereotype.Component;

/**
 * Traduce entre la entidad de persistencia (VehiculoJpaEntidad) y el modelo de dominio (Vehiculo).
 */
@Component
public class VehiculoMapeador {

    public Vehiculo toDomain(VehiculoJpaEntidad entity) {
        if (entity == null) {
            return null;
        }
        return new Vehiculo(
            entity.getId(),
            entity.getDriverId(),
            entity.getVehicleType(),
            entity.getServiceTypesSet(),
            entity.getCountryCode() != null ? entity.getCountryCode() : "AR",
            entity.getPlate(),
            entity.getMake(),
            entity.getModel(),
            entity.getModelYear(),
            entity.getQualificationStatus(),
            entity.getDriverVersion(),
            entity.getCreatedAt(), entity.getColor(), entity.getCapacity(),
            entity.getServiceCategories() == null || entity.getServiceCategories().isBlank()
                ? java.util.Set.of() : new java.util.HashSet<>(java.util.Arrays.asList(entity.getServiceCategories().split(",")))
        );
    }
}