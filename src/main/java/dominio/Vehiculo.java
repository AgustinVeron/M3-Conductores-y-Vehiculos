package m3.dominio;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Vehiculo {

    private final UUID id;
    private final UUID driverId;
    private final String vehicleType;
    private final Set<String> serviceTypes;
    private final String countryCode;
    private final String plate;
    private final String make;
    private final String model;
    private final Integer modelYear;
    private final String qualificationStatus;
    private final Long driverVersion;
    private final Instant createdAt;
    private final String color;
    private final Integer capacity;
    private final Set<String> serviceCategories;

    // Constructor compatible con HU-06
    public Vehiculo(UUID id, UUID driverId) {
        this(id, driverId, "AUTO", Set.of("AUTO"), "AR", "ABC123AA", "Generic", "Model", 2022, "PENDING", 1L, Instant.now());
    }

    public Vehiculo(
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
        this(id, driverId, vehicleType, serviceTypes, countryCode, plate, make, model, modelYear,
            qualificationStatus, driverVersion, createdAt, null, null, Set.of());
    }

    public Vehiculo(UUID id, UUID driverId, String vehicleType, Set<String> serviceTypes,
            String countryCode, String plate, String make, String model, Integer modelYear,
            String qualificationStatus, Long driverVersion, Instant createdAt,
            String color, Integer capacity, Set<String> serviceCategories) {
        this.color = color;
        this.capacity = capacity;
        this.serviceCategories = serviceCategories == null ? Set.of() : Set.copyOf(serviceCategories);
        this.id = Objects.requireNonNull(id, "vehicle id cannot be null");
        this.driverId = Objects.requireNonNull(driverId, "driver id cannot be null");
        this.vehicleType = vehicleType;
        this.serviceTypes = serviceTypes != null ? Collections.unmodifiableSet(serviceTypes) : Collections.emptySet();
        this.countryCode = countryCode;
        this.plate = plate;
        this.make = make;
        this.model = model;
        this.modelYear = modelYear;
        this.qualificationStatus = qualificationStatus != null ? qualificationStatus : "PENDING";
        this.driverVersion = driverVersion != null ? driverVersion : 1L;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public Vehiculo withQualification(HabilitacionEstado status, long version) {
        return new Vehiculo(id,driverId,vehicleType,serviceTypes,countryCode,plate,make,model,modelYear,
                status.name(),version,createdAt,color,capacity,serviceCategories);
    }
    public String getColor() { return color; }
    public Integer getCapacity() { return capacity; }
    public Set<String> getServiceCategories() { return serviceCategories; }
    public UUID getId() { return id; }
    public UUID getDriverId() { return driverId; }
    public String getVehicleType() { return vehicleType; }
    public Set<String> getServiceTypes() { return serviceTypes; }
    public String getCountryCode() { return countryCode; }
    public String getPlate() { return plate; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public Integer getModelYear() { return modelYear; }
    public String getQualificationStatus() { return qualificationStatus; }
    public Long getDriverVersion() { return driverVersion; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean belongsToDriver(UUID candidateDriverId) {
        return this.driverId.equals(candidateDriverId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehiculo vehicle = (Vehiculo) o;
        return Objects.equals(id, vehicle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}