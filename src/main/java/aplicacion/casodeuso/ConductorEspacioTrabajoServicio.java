package m3.aplicacion.casodeuso;

import m3.aplicacion.puerto.*;
import m3.aplicacion.puerto.entrada.ActualizacionMiPersonalInformacionUsoCaso;
import m3.aplicacion.puerto.salida.ActualPrincipalPuerto;
import m3.aplicacion.excepcion.ConductorAccesoDenegadoExcepcion;
import m3.dominio.*;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

/** Application boundary for the integrated web workspace. */
public class ConductorEspacioTrabajoServicio implements ActualizacionMiPersonalInformacionUsoCaso {
    private final ConductorRepositorio drivers;
    private final VehiculoRepositorio vehicles;
    private final DocumentoRepositorio documents;
    private final DisponibilidadIntencionRepositorio availability;
    private final ActualPrincipalPuerto principal;
    private final UnidadDeTrabajo transactions;
    private final Clock clock;
    public ConductorEspacioTrabajoServicio(ConductorRepositorio drivers, VehiculoRepositorio vehicles,
            DocumentoRepositorio documents, DisponibilidadIntencionRepositorio availability,
            ActualPrincipalPuerto principal, UnidadDeTrabajo transactions, Clock clock) {
        this.drivers=drivers; this.vehicles=vehicles; this.documents=documents;
        this.availability=availability; this.principal=principal; this.transactions=transactions; this.clock=clock;
    }
    public Conductor current() {
        String subject = principal.getSubject().orElseThrow(() -> new ConductorAccesoDenegadoExcepcion("No hay una sesión de conductor."));
        return drivers.findByIdentitySubject(subject).orElseThrow(() -> new IllegalArgumentException("Primero registrá tu perfil."));
    }
    public Conductor driver(UUID id) {
        Conductor d=drivers.findById(id).orElseThrow(() -> new IllegalArgumentException("No se encontró el conductor."));
        if (!isOperator() && !principal.getSubject().filter(d.getIdentitySubject()::equals).isPresent())
            throw new ConductorAccesoDenegadoExcepcion("No tenés acceso a este conductor.");
        return d;
    }
    public List<Conductor> allDrivers() { requireOperator(); return drivers.findAll(); }
    private boolean isOperator() { return principal.hasRole("OPERATOR") || principal.hasRole("OPERADOR") || principal.hasRole("ADMIN") || principal.hasRole("ADMINISTRADOR"); }
    public void requireOperator() {
        if (!isOperator()) throw new ConductorAccesoDenegadoExcepcion("Esta acción requiere un operador.");
    }
    public String actor() { return principal.getSubject().orElseThrow(() -> new ConductorAccesoDenegadoExcepcion("No hay sesión.")); }
    @Override
    public Conductor updatePersonalInfo(PersonalInformacion personalInfo, long version) {
        Objects.requireNonNull(personalInfo, "Los datos personales son requeridos.");
        if (personalInfo.getBirthDate() == null || personalInfo.getBirthDate().isAfter(java.time.LocalDate.now(clock))) {
            throw new IllegalArgumentException("Ingresá una fecha de nacimiento válida.");
        }
        required(personalInfo.getFirstName(), 100);
        required(personalInfo.getLastName(), 100);
        required(personalInfo.getDni(), 32);
        required(personalInfo.getAddress(), 255);
        required(personalInfo.getPhone(), 50);
        required(personalInfo.getEmail(), 150);
        Conductor owner = current();
        return transactions.executeInTransaction(() -> {
            Conductor locked = drivers.findByIdWithPessimisticLock(owner.getId()).orElseThrow(
                    () -> new IllegalArgumentException("No se encontró el conductor."));
            locked.validateVersion(version);
            locked.updatePersonalInfo(personalInfo, Instant.now(clock));
            return drivers.save(locked);
        });
    }
    public List<Vehiculo> vehicles(UUID id) { driver(id); return vehicles.findByDriverId(id); }
    public List<Documento> documents(UUID id) {
        driver(id);
        var result = new ArrayList<>(documents.findLicensesByDriverId(id));
        for (Vehiculo v : vehicles.findByDriverId(id)) result.addAll(documents.findByVehicleIdOrderByRevisionDesc(v.getId()));
        return List.copyOf(result);
    }
    public DisponibilidadIntencion availability(UUID id) { driver(id); return availability.findByDriverId(id).orElse(null); }
    public Vehiculo registerVehicle(String type, String plate, String make, String model, Integer year, long version) {
        return registerVehicle(type,plate,make,model,year,version,null,null,Set.of());
    }
    public Vehiculo registerVehicle(String type, String plate, String make, String model, Integer year, long version,
            String color, Integer capacity, Set<String> categories) {
        Conductor owner=current();
        if (color != null) required(color,30);
        if (capacity != null && (capacity < 1 || capacity > 6)) throw new IllegalArgumentException("La capacidad debe ser entre 1 y 6 pasajeros.");
        if (categories == null || !Set.of("STANDARD","PREMIUM","DELIVERY","PETS").containsAll(categories))
            throw new IllegalArgumentException("Seleccioná servicios válidos.");
        if (!Set.of("AUTO", "MOTO").contains(type)) throw new IllegalArgumentException("Seleccioná Auto o Moto.");
        if (year == null || year < 1886 || year > java.time.Year.now(clock).getValue()+1)
            throw new IllegalArgumentException("Ingresá un año válido.");
        String normalizedPlate=required(plate,20).toUpperCase(Locale.ROOT).replace(" ", "");
        String normalizedMake=required(make,60), normalizedModel=required(model,60);
        return transactions.executeInTransaction(() -> {
            Conductor locked=drivers.findByIdWithPessimisticLock(owner.getId()).orElseThrow();
            locked.validateVersion(version);
            if (vehicles.findByDriverId(owner.getId()).stream().anyMatch(v -> normalizedPlate.equalsIgnoreCase(v.getPlate())))
                throw new IllegalArgumentException("Ya registraste un vehículo con esa patente.");
            locked.incrementVersion(); drivers.save(locked);
            return vehicles.save(new Vehiculo(UUID.randomUUID(),owner.getId(),type,Set.of(type),"AR",
                normalizedPlate,normalizedMake,normalizedModel,year,"PENDING",(long) locked.getVersion(),Instant.now(clock),color,capacity,categories));
        });
    }
    private String required(String value,int max) {
        if(value==null || value.isBlank() || value.trim().length()>max) throw new IllegalArgumentException("Completá los campos obligatorios sin exceder su longitud máxima.");
        return value.trim();
    }
}
