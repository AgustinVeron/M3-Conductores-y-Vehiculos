package m3.aplicacion.casodeuso;
import m3.dominio.GuardadoClienteCalificacion;
import m3.aplicacion.puerto.*;
import m3.dominio.*;
import m3.dominio.excepcion.*;
import java.time.Clock;
import java.util.*;
public class ClienteCalificacionServicio {
    private final ConductorAccesoServicio access; private final ConductorRepositorio drivers;
    private final ClienteCalificacionRepositorio ratings; private final ViajeContextoPuerto trips;
    private final UnidadDeTrabajo transactions; private final Clock clock;
    public ClienteCalificacionServicio(ConductorAccesoServicio access,ConductorRepositorio drivers,ClienteCalificacionRepositorio ratings,
            ViajeContextoPuerto trips,UnidadDeTrabajo transactions,Clock clock) {
        this.access=access; this.drivers=drivers; this.ratings=ratings; this.trips=trips; this.transactions=transactions; this.clock=clock;
    }
    public ClienteCalificacion rate(UUID driverId,String tripId,int score,String comment,String key) {
        access.own(driverId);
        if(tripId==null || tripId.isBlank() || tripId.length()>128 || score<1 || score>5 || (comment!=null && comment.length()>500))
            throw new IllegalArgumentException("Indicá un viaje, una puntuación de 1 a 5 y hasta 500 caracteres de comentario.");
        if(key==null || key.length()<16 || key.length()>128) throw new IllegalArgumentException("Idempotency-Key debe tener entre 16 y 128 caracteres.");
        String fingerprint=tripId+":"+score+":"+(comment==null?"":comment);
        var prior=ratings.findByKey(driverId,key);
        if(prior.isPresent()) return replay(prior.get(),fingerprint);
        // The external request is deliberately outside the database transaction.
        var context=trips.completedTrip(tripId,driverId);
        if(context==null || !tripId.equals(context.tripId()) || !driverId.equals(context.driverId()) || !"COMPLETED".equals(context.status())
                || context.clientId()==null || context.clientId().isBlank() || context.clientId().length()>128)
            throw new DominioValidacionExcepcion("M6 no confirmó un viaje completado y asignado a este conductor.");
        return transactions.executeInTransaction(() -> {
            drivers.findByIdWithPessimisticLock(driverId).orElseThrow(() -> new ConductorNoEncontradoExcepcion(driverId));
            var existing=ratings.findByKey(driverId,key);
            if(existing.isPresent()) return replay(existing.get(),fingerprint);
            if(ratings.findByTrip(driverId,tripId).isPresent()) throw new IdempotenciaConflictoExcepcion("Ya valoraste este viaje.");
            ClienteCalificacion rating=new ClienteCalificacion(UUID.randomUUID(),driverId,tripId,context.clientId(),score,comment,clock.instant());
            ratings.save(rating,key,fingerprint); return rating;
        });
    }
    private ClienteCalificacion replay(GuardadoClienteCalificacion saved,String fingerprint) {
        if(!saved.fingerprint().equals(fingerprint)) throw new IdempotenciaConflictoExcepcion("La clave ya se utilizó para otra valoración.");
        return saved.rating();
    }
    public List<ClienteCalificacion> history(UUID driverId,int offset,int limit) {
        access.driver(driverId);
        if(offset<0 || limit<1 || limit>100) throw new IllegalArgumentException("Paginación inválida.");
        return ratings.history(driverId,offset,limit);
    }
}
