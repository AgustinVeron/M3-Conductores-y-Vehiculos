package m3.aplicacion.casodeuso;
import m3.aplicacion.puerto.*;
import m3.dominio.*;
import m3.dominio.excepcion.*;
import java.time.*;
import java.util.*;

public class HabilitacionServicio {
    private final ConductorRepositorio drivers;
    private final VehiculoRepositorio vehicles;
    private final DocumentoRepositorio documents;
    private final HabilitacionRepositorio history;
    private final AuditoriaRegistroRepositorio audit;
    private final UnidadDeTrabajo transactions;
    private final ConductorAccesoServicio access;
    private final Clock clock;
    public HabilitacionServicio(ConductorRepositorio drivers, VehiculoRepositorio vehicles, DocumentoRepositorio documents,
            HabilitacionRepositorio history, AuditoriaRegistroRepositorio audit, UnidadDeTrabajo transactions, ConductorAccesoServicio access, Clock clock) {
        this.drivers=drivers; this.vehicles=vehicles; this.documents=documents; this.history=history;
        this.audit=audit; this.transactions=transactions; this.access=access; this.clock=clock;
    }
    public HabilitacionDecision decide(UUID driverId, UUID vehicleId, HabilitacionEstado decision,
            String reason, long expectedVersion, String key) {
        access.requireOperator(); access.driver(driverId);
        if (decision == null || decision == HabilitacionEstado.PENDING) throw new DominioValidacionExcepcion("Seleccioná una decisión administrativa válida.");
        if (reason == null || reason.isBlank() || reason.length()>500) throw new DominioValidacionExcepcion("Ingresá un motivo de 1 a 500 caracteres.");
        if (key==null || key.length()<16 || key.length()>128) throw new IllegalArgumentException("Idempotency-Key debe tener entre 16 y 128 caracteres.");
        String actor=access.actor(), fingerprint=decision.name()+":"+reason.trim();
        return transactions.executeInTransaction(() -> {
            Conductor driver=drivers.findByIdWithPessimisticLock(driverId).orElseThrow(() -> new ConductorNoEncontradoExcepcion(driverId));
            Vehiculo vehicle=vehicleId==null ? null : vehicles.findByIdAndDriverId(vehicleId,driverId).orElseThrow(() -> new VehiculoNoEncontradoExcepcion(vehicleId));
            var previous=history.findByKey(vehicleId==null ? driverId : vehicleId,actor,key);
            if(previous.isPresent()) {
                if(!previous.get().fingerprint().equals(fingerprint)) throw new IdempotenciaConflictoExcepcion("La clave ya se usó con otra decisión.");
                return previous.get().decision();
            }
            driver.validateVersion(expectedVersion);
            HabilitacionEstado from=vehicle==null ? driver.getQualificationStatus() : HabilitacionEstado.valueOf(vehicle.getQualificationStatus());
            if(from==decision) throw new DominioValidacionExcepcion("El objeto ya tiene ese estado.");
            if(decision!=HabilitacionEstado.SUSPENDED) {
                boolean allowed=switch(from) {
                    case PENDING -> decision==HabilitacionEstado.APPROVED || decision==HabilitacionEstado.OBSERVED || decision==HabilitacionEstado.REJECTED;
                    case APPROVED -> decision==HabilitacionEstado.OBSERVED;
                    case OBSERVED,REJECTED -> false;
                    case SUSPENDED -> true;
                };
                if(!allowed) throw new DominioValidacionExcepcion("La transición requiere corregir y volver a presentar la documentación.");
            }
            Instant now=clock.instant();
            if(decision==HabilitacionEstado.APPROVED) {
                if(vehicle==null) requireValid(documents.findCurrentLicenseByDriverId(driverId).orElse(null),now,"licencia");
                else {
                    requireValid(documents.findCurrentByVehicleIdAndType(vehicleId,DocumentoTipo.INSURANCE).orElse(null),now,"seguro");
                    requireValid(documents.findCurrentByVehicleIdAndType(vehicleId,DocumentoTipo.VEHICLE_REGISTRATION).orElse(null),now,"cédula");
                }
            }
            if(vehicle==null) driver.updateQualification(new HabilitacionInformacion(ConductorEstado.valueOf(decision.name()),reason.trim()),now);
            else { driver.incrementVersion(); vehicles.save(vehicle.withQualification(decision,driver.getVersion())); }
            drivers.save(driver);
            HabilitacionDecision result=new HabilitacionDecision(UUID.randomUUID(),driverId,vehicleId,from,decision,reason.trim(),actor,driver.getVersion(),now);
            history.save(result,key,fingerprint);
            audit.save(new AuditoriaRegistro(UUID.randomUUID(),vehicle==null?"DRIVER":"VEHICLE",vehicleId==null?driverId:vehicleId,
                    driverId,"QUALIFICATION",actor,from.name(),decision.name(),reason.trim(),null,now));
            return result;
        });
    }
    public List<HabilitacionDecision> history(UUID driverId, UUID vehicleId, int offset, int limit) {
        access.driver(driverId);
        if(vehicleId!=null && !vehicles.existsByIdAndDriverId(vehicleId,driverId)) throw new VehiculoNoEncontradoExcepcion(vehicleId);
        if(offset<0 || limit<1 || limit>100) throw new IllegalArgumentException("Paginación inválida.");
        return history.history(driverId,vehicleId,offset,limit);
    }
    public static void requireValid(Documento doc, Instant now, String name) {
        if(doc==null || !doc.isCurrent() || doc.getStatus()!=DocumentoEstado.APPROVED || doc.getIssuedAt().isAfter(now) || !doc.getValidUntil().isAfter(now))
            throw new DominioValidacionExcepcion("Se requiere "+name+" actual, aprobado y vigente.");
    }
}
