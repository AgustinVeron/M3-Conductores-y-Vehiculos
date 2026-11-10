package m3.adaptador.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class VehiculoRegistroFormulario {
    @NotBlank(message = "La marca es obligatoria.")
    @Size(max = 60, message = "La marca no puede superar los 60 caracteres.")
    private String brand;
    private long version;
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }

    @NotBlank(message = "El modelo es obligatorio.")
    @Size(max = 60, message = "El modelo no puede superar los 60 caracteres.")
    private String model;

    @NotNull(message = "El año es obligatorio.")
    @Min(value = 1886, message = "Ingresá un año válido.")
    @Max(value = 2100, message = "Ingresá un año válido.")
    private Integer year;

    @NotBlank(message = "La patente es obligatoria.")
    @Size(max = 20, message = "La patente no puede superar los 20 caracteres.")
    private String plate;

    @NotBlank(message = "El color es obligatorio.")
    @Size(max = 30, message = "El color no puede superar los 30 caracteres.")
    private String color;

    @NotNull(message = "La capacidad es obligatoria.")
    @Min(value = 1, message = "La capacidad debe ser como mínimo 1 pasajero.")
    @Max(value = 6, message = "La capacidad máxima es de 6 pasajeros.")
    private Integer capacity;

    @NotEmpty(message = "Seleccioná al menos un tipo de servicio.")
    private List<String> services = new ArrayList<>();

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }
}