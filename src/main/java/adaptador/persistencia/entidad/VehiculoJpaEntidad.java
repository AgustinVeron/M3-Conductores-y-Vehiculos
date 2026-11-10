package m3.adaptador.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA Entity mapping the 'vehicles' table.
 */
@Entity
@Table(name = "vehicles")
public class VehiculoJpaEntidad {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "service_types", length = 50)
    private String serviceTypes;

    @Column(name = "country_code", length = 2)
    private String countryCode = "AR";

    @Column(name = "plate")
    private String plate;

    @Column(name = "make")
    private String make;

    @Column(name = "model")
    private String model;

    @Column(name = "model_year")
    private Integer modelYear;

    @Column(name = "qualification_status")
    private String qualificationStatus;

    @Column(name = "driver_version")
    private Long driverVersion;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "color", length = 30)
    private String color;
    @Column(name = "capacity")
    private Integer capacity;
    @Column(name = "service_categories", length = 100)
    private String serviceCategories;
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getServiceCategories() { return serviceCategories; }
    public void setServiceCategories(String categories) { this.serviceCategories = categories; }

    public VehiculoJpaEntidad() {
    }

    public VehiculoJpaEntidad(
        UUID id,
        UUID driverId,
        String vehicleType,
        String serviceTypes,
        String countryCode,
        String plate,
        String make,
        String model,
        Integer modelYear,
        String qualificationStatus,
        Long driverVersion,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.driverId = driverId;
        this.vehicleType = vehicleType;
        this.serviceTypes = serviceTypes;
        this.countryCode = countryCode != null ? countryCode : "AR";
        this.plate = plate;
        this.make = make;
        this.model = model;
        this.modelYear = modelYear;
        this.qualificationStatus = qualificationStatus;
        this.driverVersion = driverVersion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor compatible con Set<String> (HU-04)
    public VehiculoJpaEntidad(
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
        Instant createdAt
    ) {
        this(
            id,
            driverId,
            vehicleType,
            serviceTypes != null ? String.join(",", serviceTypes) : null,
            countryCode,
            plate,
            make,
            model,
            modelYear,
            qualificationStatus,
            driverVersion,
            createdAt,
            null
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getServiceTypes() { return serviceTypes; }
    public void setServiceTypes(String serviceTypes) { this.serviceTypes = serviceTypes; }

    public Set<String> getServiceTypesSet() {
        if (serviceTypes == null || serviceTypes.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(serviceTypes.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }

    public String getQualificationStatus() { return qualificationStatus; }
    public void setQualificationStatus(String qualificationStatus) { this.qualificationStatus = qualificationStatus; }

    public Long getDriverVersion() { return driverVersion; }
    public void setDriverVersion(Long driverVersion) { this.driverVersion = driverVersion; }

    public Long getVersion() { return driverVersion; }
    public void setVersion(Long version) { this.driverVersion = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}