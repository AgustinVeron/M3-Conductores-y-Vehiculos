package m3.dominio.elegibilidad;
import m3.dominio.HabilitacionEstado;
import java.time.Instant;
import java.util.*;
public class ElegibilidadVerificador {
    public ElegibilidadResultado check(ConductorInstantanea driver,Instant now) {
        List<String> reasons=new ArrayList<>();
        if(driver.qualificationStatus()==HabilitacionEstado.SUSPENDED) reasons.add(ElegibilidadMotivo.DRIVER_SUSPENDED);
        else if(driver.qualificationStatus()!=HabilitacionEstado.APPROVED) reasons.add(ElegibilidadMotivo.DRIVER_NOT_APPROVED);
        Instant until=checkDocument(driver.license(),"LICENSE",now,reasons);
        var vehicle=driver.selectedVehicle();
        if(vehicle==null) reasons.add(ElegibilidadMotivo.NO_VEHICLE_SELECTED);
        else {
            if(vehicle.qualificationStatus()==HabilitacionEstado.SUSPENDED) reasons.add(ElegibilidadMotivo.VEHICLE_SUSPENDED);
            else if(vehicle.qualificationStatus()!=HabilitacionEstado.APPROVED) reasons.add(ElegibilidadMotivo.VEHICLE_NOT_APPROVED);
            until=min(until,checkDocument(vehicle.insurance(),"INSURANCE",now,reasons));
            until=min(until,checkDocument(vehicle.registration(),"REGISTRATION",now,reasons));
            if(driver.license()!=null && (driver.license().serviceTypes()==null || vehicle.serviceTypes()==null || Collections.disjoint(driver.license().serviceTypes(),vehicle.serviceTypes())))
                reasons.add(ElegibilidadMotivo.SERVICE_TYPE_NOT_PERMITTED);
        }
        return new ElegibilidadResultado(reasons.isEmpty(),List.copyOf(reasons),now,driver.version(),reasons.isEmpty()?until:now);
    }
    private Instant checkDocument(DocumentoInstantanea doc,String prefix,Instant now,List<String> reasons) {
        if(doc==null) { reasons.add(prefix+"_MISSING"); return null; }
        if(doc.qualificationStatus()!=HabilitacionEstado.APPROVED) reasons.add(prefix+"_NOT_APPROVED");
        else if(doc.validUntil()==null || doc.isExpiredAt(now)) reasons.add(prefix+"_EXPIRED");
        else if(doc.issuedAt()==null || doc.issuedAt().isAfter(now)) reasons.add(prefix+"_NOT_YET_VALID");
        return doc.validUntil();
    }
    private Instant min(Instant a,Instant b) { return a==null?b:b==null?a:a.isBefore(b)?a:b; }
}
