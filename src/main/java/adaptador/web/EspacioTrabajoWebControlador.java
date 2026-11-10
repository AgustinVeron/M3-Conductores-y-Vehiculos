package m3.adaptador.web;

import m3.aplicacion.casodeuso.*;
import m3.aplicacion.excepcion.ConductorAccesoDenegadoExcepcion;
import m3.adaptador.seguridad.DesarrolloModo;
import m3.adaptador.seguridad.DemostracionAccesoAutenticacionFiltro;
import m3.aplicacion.ubicacion.UbicacionSimulacionServicio;
import m3.aplicacion.viaje.ConductorOferta;
import m3.aplicacion.viaje.ConductorViaje;
import m3.aplicacion.viaje.ConductorViajeFlujoServicio;
import m3.dominio.*;
import m3.dominio.excepcion.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EspacioTrabajoWebControlador {
    private final HabilitacionServicio qualification;
    private final ClienteCalificacionServicio ratings;
    private final m3.aplicacion.puerto.DisponibilidadEntregaPuerto delivery;
    private final ConductorEspacioTrabajoServicio workspace;
    private final SolicitudDisponibilidadUsoCaso availability;
    private final RegistrarVehiculoDocumentoUsoCaso vehicleDocuments;
    private final RevisionDocumentoUsoCaso reviews;
    private final CrearConductorPerfilUsoCaso createDriver;
    private final DesarrolloModo developmentMode;
    private final ConductorViajeFlujoServicio tripWorkflow;
    private final UbicacionSimulacionServicio locationSimulation;
    @Value("${app.demo-access.enabled:false}") private boolean demoAccessEnabled;
    @Value("${app.demo-access.subject:}") private String demoAccessSubject;
    public EspacioTrabajoWebControlador(ConductorEspacioTrabajoServicio workspace, SolicitudDisponibilidadUsoCaso availability,
            RegistrarVehiculoDocumentoUsoCaso vehicleDocuments, RevisionDocumentoUsoCaso reviews, CrearConductorPerfilUsoCaso createDriver, HabilitacionServicio qualification, ClienteCalificacionServicio ratings, m3.aplicacion.puerto.DisponibilidadEntregaPuerto delivery, DesarrolloModo developmentMode,
            ConductorViajeFlujoServicio tripWorkflow, UbicacionSimulacionServicio locationSimulation) {
        this.qualification=qualification; this.ratings=ratings; this.delivery=delivery;
        this.workspace=workspace; this.availability=availability; this.vehicleDocuments=vehicleDocuments;
        this.reviews=reviews; this.createDriver=createDriver; this.developmentMode=developmentMode;
        this.tripWorkflow=tripWorkflow; this.locationSimulation=locationSimulation;
    }
    @GetMapping("/") public String root(Model model) { return access(model,false); }
    @GetMapping("/login/m1") public String m1Unavailable(Model model) { return access(model,true); }
    @GetMapping("/demo/access") public String demoAccess(jakarta.servlet.http.HttpServletRequest request) {
        startDemoSession(request);
        return "redirect:/ui/profile";
    }
    @GetMapping("/demo/operator") public String demoOperator(jakarta.servlet.http.HttpServletRequest request) {
        startDemoSession(request);
        return "redirect:/ui/operator/drivers";
    }
    @GetMapping("/ui/register") public String registration(Model model) { return page(model,"register","Crear mi perfil"); }
    @PostMapping("/ui/register") public String register(@RequestParam String name, RedirectAttributes flash) {
        if(name.isBlank() || name.length()>100) throw new IllegalArgumentException("Ingresá un nombre de hasta 100 caracteres.");
        createDriver.execute(workspace.actor(),name);
        flash.addFlashAttribute("success","Perfil registrado. Quedó pendiente de revisión.");
        return "redirect:/ui/profile";
    }
    @GetMapping("/ui/vehicles") public String vehicles(Model model) {
        Conductor driver=workspace.current(); model.addAttribute("driver",driver);
        model.addAttribute("vehicles",workspace.vehicles(driver.getId()));
        return page(model,"vehicles","Mis vehículos");
    }
    @PostMapping("/ui/vehicles") public String registerVehicle(@RequestParam String type, @RequestParam String plate,
            @RequestParam String make, @RequestParam String model, @RequestParam Integer year,
            @RequestParam long version, RedirectAttributes flash) {
        try { workspace.registerVehicle(type,plate,make,model,year,version);
            flash.addFlashAttribute("success","Vehículo registrado. Quedó pendiente de revisión.");
        } catch(IllegalArgumentException | VersionInconsistenciaExcepcion ex) { flash.addFlashAttribute("error",ex.getMessage()); }
        return "redirect:/ui/vehicles";
    }
    @GetMapping("/ui/documents") public String documents(Model model) {
        Conductor driver=workspace.current(); model.addAttribute("driver",driver);
        model.addAttribute("vehicles",workspace.vehicles(driver.getId()));
        model.addAttribute("documents",workspace.documents(driver.getId()));
        return page(model,"documents","Mi documentación");
    }
    @PostMapping("/ui/documents/vehicle") public String document(@RequestParam UUID vehicleId,
            @RequestParam DocumentoTipo documentType, @RequestParam String documentNumber,
            @RequestParam String issuingEntity, @RequestParam LocalDate issuedAt, @RequestParam LocalDate validUntil,
            RedirectAttributes flash) {
        try {
            if(documentType==DocumentoTipo.LICENSE) throw new IllegalArgumentException("Usá el formulario de licencia del conductor.");
            if(documentNumber.isBlank() || documentNumber.length()>80 || issuingEntity.length()>100)
                throw new IllegalArgumentException("Revisá el número de documento y la entidad emisora.");
            ZoneId zone=ZoneId.of("America/Argentina/Buenos_Aires");
            vehicleDocuments.registerVehicleDocument(new RegistrarVehiculoDocumentoComando(workspace.current().getId(),vehicleId,
                    documentType,documentNumber,issuingEntity,issuedAt.atStartOfDay(zone).toInstant(),
                    validUntil.plusDays(1).atStartOfDay(zone).toInstant(),null));
            flash.addFlashAttribute("success","Documento registrado y pendiente de revisión.");
        } catch(IllegalArgumentException | DominioValidacionExcepcion | VehiculoProhibidoExcepcion | VehiculoNoEncontradoExcepcion ex) {
            flash.addFlashAttribute("error",ex.getMessage());
        }
        return "redirect:/ui/documents";
    }
    @GetMapping("/ui/availability") public String availability(Model model) {
        Conductor driver=workspace.current(); model.addAttribute("driver",driver);
        model.addAttribute("vehicles",workspace.vehicles(driver.getId()));
        model.addAttribute("intent",workspace.availability(driver.getId()));
        model.addAttribute("idempotencyKey",UUID.randomUUID().toString());
        return page(model,"availability","Disponibilidad");
    }
    @PostMapping("/ui/availability") public String availability(@RequestParam UUID vehicleId,
            @RequestParam(defaultValue="false") boolean connected, @RequestParam(defaultValue="false") boolean available,
            @RequestParam String idempotencyKey, RedirectAttributes flash) throws java.security.NoSuchAlgorithmException {
        try {
            if(idempotencyKey.length()<16 || idempotencyKey.length()>128) throw new IllegalArgumentException("Recargá el formulario antes de enviarlo.");
            UUID driverId=workspace.current().getId();
            String body=driverId+":"+vehicleId+":"+connected+":"+available;
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8)));
            availability.execute(new SolicitudDisponibilidadComando(driverId,vehicleId,connected,available,idempotencyKey,hash,
                    workspace.actor(),UUID.randomUUID().toString()));
            flash.addFlashAttribute("success","Solicitud guardada. La confirmación de M4 sigue pendiente.");
        } catch(IllegalArgumentException | DominioValidacionExcepcion | ConductorNoHabilitadoExcepcion | IdempotenciaConflictoExcepcion ex) {
            flash.addFlashAttribute("error",ex.getMessage());
        }
        return "redirect:/ui/availability";
    }
    @GetMapping("/ui/operator/drivers") public String operators(Model model) {
        model.addAttribute("drivers",workspace.allDrivers()); return page(model,"operator","Conductores");
    }
    @GetMapping("/ui/operator/drivers/{id}") public String detail(@PathVariable UUID id,Model model) {
        workspace.requireOperator();
        Conductor driver=workspace.driver(id);
        var vehicles=workspace.vehicles(id);
        var documents=workspace.documents(id);
        model.addAttribute("driver",driver);
        model.addAttribute("vehicles",vehicles);
        model.addAttribute("documents",documents);
        model.addAttribute("driverStatusLabel",OperadorRevisionVistaModelo.statusLabel(
                driver.getQualificationStatus()!=null ? driver.getQualificationStatus().name() : null));
        model.addAttribute("review",OperadorRevisionVistaModelo.from(vehicles,documents));
        var decisions=new ArrayList<>(qualification.history(id,null,0,100));
        for(var v:vehicles) decisions.addAll(qualification.history(id,v.getId(),0,100));
        decisions.sort(java.util.Comparator.comparing(HabilitacionDecision::occurredAt).reversed());
        model.addAttribute("decisions",decisions);
        model.addAttribute("idempotencyKey",UUID.randomUUID().toString());
        return page(model,"operator-detail","Revisión del conductor");
    }
    @PostMapping("/ui/operator/drivers/{id}/documents/{documentId}/review")
    public String review(@PathVariable UUID id,@PathVariable UUID documentId,@RequestParam RevisionDecision decision,
            @RequestParam(defaultValue="") String reason,@RequestParam long version,RedirectAttributes flash) {
        workspace.requireOperator(); workspace.driver(id);
        try {
            reviews.reviewDocument(new RevisionDocumentoComando(id,documentId,decision,reason.isBlank() ? null : reason,version,workspace.actor(),UUID.randomUUID().toString()));
            flash.addFlashAttribute("success","Revisión guardada.");
        } catch(IllegalArgumentException | DominioValidacionExcepcion | VersionInconsistenciaExcepcion ex) {
            flash.addFlashAttribute("error",ex.getMessage());
        }
        return "redirect:/ui/operator/drivers/"+id;
    }
    @PostMapping("/ui/operator/drivers/{id}/qualification")
    public String qualify(@PathVariable UUID id,@RequestParam(required=false) UUID vehicleId,
            @RequestParam HabilitacionEstado status,@RequestParam String reason,@RequestParam long version,
            @RequestParam String idempotencyKey,RedirectAttributes flash) {
        try { qualification.decide(id,vehicleId,status,reason,version,idempotencyKey); flash.addFlashAttribute("success","Decisión guardada con su historial."); }
        catch(DominioValidacionExcepcion | IllegalArgumentException | VersionInconsistenciaExcepcion | IdempotenciaConflictoExcepcion ex) { flash.addFlashAttribute("error",ex.getMessage()); }
        return "redirect:/ui/operator/drivers/"+id;
    }
    @PostMapping("/ui/availability/retry")
    public String retry(RedirectAttributes flash) {
        delivery.retry(workspace.current().getId(),java.time.Instant.now());
        flash.addFlashAttribute("success","Se programó el reintento de la última solicitud fallida.");
        return "redirect:/ui/availability";
    }
    @GetMapping("/ui/offers") public String offers(Model model) {
        UUID driverId=workspace.current().getId();
        model.addAttribute("offers",tripWorkflow.offersFor(driverId));
        return page(model,"offers","Pedidos recibidos");
    }
    @PostMapping("/ui/offers/{offerId}/decision") public String decideOffer(@PathVariable UUID offerId,
            @RequestParam String decision,RedirectAttributes flash) {
        try {
            ConductorOferta updated=tripWorkflow.respond(workspace.current().getId(),offerId,decision);
            flash.addFlashAttribute("success",updated.status()==ConductorOferta.OfferStatus.ACCEPTED
                    ? "Pedido aceptado. Ya tenés un viaje asignado." : "Pedido rechazado.");
        } catch(DominioValidacionExcepcion | IntegracionNoDisponibleExcepcion | IllegalArgumentException ex) {
            flash.addFlashAttribute("error",ex.getMessage());
        }
        return "redirect:/ui/offers";
    }
    @GetMapping("/ui/trip") public String trip(Model model) {
        UUID driverId=workspace.current().getId();
        model.addAttribute("trip",tripWorkflow.currentTrip(driverId));
        model.addAttribute("location",locationSimulation.currentLocation(driverId));
        model.addAttribute("ratingSummary",tripWorkflow.ratingSummary(driverId));
        return page(model,"trip","Viaje actual");
    }
    @GetMapping("/ui/trip/status") @ResponseBody public TripStatusPayload tripStatus() {
        UUID driverId=workspace.current().getId();
        return new TripStatusPayload(tripWorkflow.currentTrip(driverId),locationSimulation.currentLocation(driverId));
    }
    @PostMapping("/ui/trip/{tripId}/start") public String startTrip(@PathVariable String tripId,RedirectAttributes flash) {
        try { tripWorkflow.start(workspace.current().getId(),tripId); flash.addFlashAttribute("success","Viaje iniciado."); }
        catch(DominioValidacionExcepcion | IntegracionNoDisponibleExcepcion | IllegalArgumentException ex) { flash.addFlashAttribute("error",ex.getMessage()); }
        return "redirect:/ui/trip";
    }
    @PostMapping("/ui/trip/{tripId}/complete") public String completeTrip(@PathVariable String tripId,RedirectAttributes flash) {
        try { tripWorkflow.requestCompletion(workspace.current().getId(),tripId); flash.addFlashAttribute("success","Se solicitó a M6 finalizar el viaje."); }
        catch(DominioValidacionExcepcion | IntegracionNoDisponibleExcepcion | IllegalArgumentException ex) { flash.addFlashAttribute("error",ex.getMessage()); }
        return "redirect:/ui/trip";
    }
    @GetMapping("/ui/customer-ratings") public String ratings(Model model) {
        Conductor driver=workspace.current(); model.addAttribute("ratings",ratings.history(driver.getId(),0,100));
        model.addAttribute("idempotencyKey",UUID.randomUUID().toString());
        return page(model,"ratings","Valoraciones");
    }
    @PostMapping("/ui/customer-ratings") public String rate(@RequestParam String tripId,@RequestParam int score,
            @RequestParam(defaultValue="") String comment,@RequestParam String idempotencyKey,RedirectAttributes flash) {
        try { ratings.rate(workspace.current().getId(),tripId,score,comment,idempotencyKey); flash.addFlashAttribute("success","Valoración registrada."); }
        catch(DominioValidacionExcepcion | IllegalArgumentException | IntegracionNoDisponibleExcepcion | IdempotenciaConflictoExcepcion ex) { flash.addFlashAttribute("error",ex.getMessage()); }
        return "redirect:/ui/customer-ratings";
    }
    @ExceptionHandler({IllegalArgumentException.class, ConductorAccesoDenegadoExcepcion.class, m3.aplicacion.excepcion.ConductorYaExisteExcepcion.class})
    public String error(RuntimeException ex,Model model) {
        model.addAttribute("error",ex.getMessage()); return page(model,"error","No se pudo completar la operación");
    }
    private String page(Model model,String page,String title) { model.addAttribute("page",page); model.addAttribute("title",title); return "espacio-conductor"; }
    private String access(Model model,boolean m1Unavailable) {
        model.addAttribute("development",developmentMode.enabled());
        model.addAttribute("m1Unavailable",m1Unavailable && developmentMode.enabled());
        model.addAttribute("demoAccessAvailable",demoAccessEnabled && !demoAccessSubject.isBlank());
        return "acceso";
    }
    private void startDemoSession(jakarta.servlet.http.HttpServletRequest request) {
        if(!demoAccessEnabled || demoAccessSubject.isBlank()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        var session=request.getSession(true);
        // Reemplaza una sesión M1 previa para volver al recorrido completo de demostración.
        session.removeAttribute("SPRING_SECURITY_CONTEXT");
        session.setAttribute(DemostracionAccesoAutenticacionFiltro.SUBJECT_ATTRIBUTE,demoAccessSubject);
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
    public record TripStatusPayload(ConductorViaje trip, UbicacionSimulacionServicio.LocationSnapshot location) {}
}
