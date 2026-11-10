package m3.adaptador.api.dto;

import m3.dominio.Vehiculo;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * DTO de respuesta para un vehículo según el esquema OpenAPI.
 */
public record VehiculoRespuesta(
    UUID id,
    UUID driverId,
    String vehicleType,
    Set<String> serviceTypes,
    String countryCode,
    String plate,
    String make,
    String model,
    Integer modelYear,
    String qualificationStatus,
    Long driverVersion,
    Instant createdAt,
    String color,
    Integer capacity,
    Set<String> serviceCategories
) {
    public static VehiculoRespuesta fromDomain(Vehiculo v) {
        return new VehiculoRespuesta(
            v.getId(),
            v.getDriverId(),
            v.getVehicleType(),
            v.getServiceTypes(),
            v.getCountryCode(),
            v.getPlate(),
            v.getMake(),
            v.getModel(),
            v.getModelYear(),
            v.getQualificationStatus(),
            v.getDriverVersion(),
            v.getCreatedAt(), v.getColor(), v.getCapacity(), v.getServiceCategories()
        );
    }
}