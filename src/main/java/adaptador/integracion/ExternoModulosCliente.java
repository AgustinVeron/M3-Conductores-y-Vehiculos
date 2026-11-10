package m3.adaptador.integracion;
import m3.dominio.ViajeContexto;
import m3.dominio.EntregaResultado;
import m3.dominio.DisponibilidadMensaje;
import m3.aplicacion.puerto.*;
import m3.aplicacion.ubicacion.UbicacionActualizacion;
import m3.aplicacion.viaje.ConductorOferta;
import m3.aplicacion.viaje.OfertaDecisionComprobante;
import m3.dominio.excepcion.*;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
@Component
public class ExternoModulosCliente implements M4DisponibilidadPuerto, M4UbicacionPuerto, M5OfertaPuerto,
        M6ViajeCicloVidaPuerto, ViajeContextoPuerto {
    private final RestClient client; private final ServicioTokenProveedor tokens; private final String m4,m5,m6;
    @org.springframework.beans.factory.annotation.Autowired
    public ExternoModulosCliente(ServicioTokenProveedor tokens,@Value("${m3.integration.m4-base-url:http://localhost:4004}") String m4,
            @Value("${m3.integration.m5-base-url:http://localhost:5005}") String m5,
            @Value("${m3.integration.m6-base-url:http://localhost:6006}") String m6) {
        this.tokens=tokens;this.m4=m4;this.m5=m5;this.m6=m6;
        var factory=new JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));client=RestClient.builder().requestFactory(factory).build();
    }
    public ExternoModulosCliente(ServicioTokenProveedor tokens, String m4, String m6) {
        this(tokens, m4, "http://localhost:5005", m6);
    }
    public EntregaResultado send(DisponibilidadMensaje m) {
        try {
            String token=tokens.token("m4:availability:write");
            var response=client.put().uri(m4+"/api/v1/drivers/{driverId}/availability-intent",m.driverId())
                .headers(h->{if(token!=null)h.setBearerAuth(token);h.set("Idempotency-Key",m.id().toString());})
                .contentType(MediaType.APPLICATION_JSON).body(m.payload()).retrieve().toEntity(Acknowledgement.class);
            var ack=response.getBody();
            return response.getStatusCode().value()==200 && ack!=null && ack.appliedVersion()==m.version() ? EntregaResultado.APPLIED : EntregaResultado.RETRY;
        } catch(RestClientResponseException ex) {
            int status=ex.getStatusCode().value();return status==429 || status>=500 || status==401 ? EntregaResultado.RETRY : EntregaResultado.REJECTED;
        } catch(RuntimeException ex) { return EntregaResultado.RETRY; }
    }
    @Override
    public void sendLocation(UbicacionActualizacion update) {
        try {
            String token=tokens.token("m4:location:write");
            client.put().uri(m4+"/api/v1/drivers/{driverId}/location",update.driverId())
                .headers(h->{if(token!=null)h.setBearerAuth(token);})
                .contentType(MediaType.APPLICATION_JSON).body(update).retrieve().toBodilessEntity();
        } catch (RestClientException ex) {
            throw new IntegracionNoDisponibleExcepcion("M4 no está disponible para recibir la ubicación.");
        }
    }
    @Override
    public OfertaDecisionComprobante respondToOffer(UUID offerId, UUID driverId, String decision) {
        try {
            String token=tokens.token("m5:offers:respond");
            String idempotency=UUID.nameUUIDFromBytes((offerId+":"+driverId+":"+decision).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
            return client.post().uri(m5+"/api/v1/offers/{offerId}/driver-response",offerId)
                .headers(h->{if(token!=null)h.setBearerAuth(token);h.set("Idempotency-Key",idempotency);})
                .contentType(MediaType.APPLICATION_JSON).body(new M5Decision(driverId,decision))
                .retrieve().body(OfertaDecisionComprobante.class);
        } catch (RestClientException ex) {
            throw new IntegracionNoDisponibleExcepcion("No fue posible responder la oferta en M5.");
        }
    }
    @Override
    public void startTrip(String tripId, UUID driverId) {
        sendTripAction(tripId, driverId, "start");
    }
    @Override
    public void requestTripCompletion(String tripId, UUID driverId) {
        sendTripAction(tripId, driverId, "complete");
    }
    private void sendTripAction(String tripId, UUID driverId, String action) {
        try {
            String token=tokens.token("m6:trip:lifecycle:write");
            String idempotency=UUID.nameUUIDFromBytes((tripId+":"+action).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
            client.post().uri(m6+"/api/v1/trips/{tripId}/{action}",tripId,action)
                .headers(h->{if(token!=null)h.setBearerAuth(token);h.set("Idempotency-Key",idempotency);})
                .contentType(MediaType.APPLICATION_JSON).body(new M6TripAction(driverId))
                .retrieve().toBodilessEntity();
        } catch (RestClientException ex) {
            throw new IntegracionNoDisponibleExcepcion(action.equals("start")
                    ? "No fue posible iniciar el viaje en M6." : "No fue posible solicitar la finalización en M6.");
        }
    }
    public ViajeContexto completedTrip(String tripId,UUID driverId) {
        try {
            String token=tokens.token("m6:rating-context:read");
            return client.get().uri(m6+"/api/v1/trips/{tripId}/rating-context?driverId={driverId}",tripId,driverId)
                .headers(h->{if(token!=null)h.setBearerAuth(token);}).retrieve().body(ViajeContexto.class);
        } catch(RestClientResponseException ex) {
            if(ex.getStatusCode().value()==404 || ex.getStatusCode().value()==422) throw new DominioValidacionExcepcion("M6 no reconoce un viaje completado para esa valoración.");
            throw new IntegracionNoDisponibleExcepcion("M6 no está disponible para verificar el viaje. Intentá nuevamente.");
        } catch(RestClientException ex) { throw new IntegracionNoDisponibleExcepcion("No se pudo conectar con M6 para verificar el viaje."); }
    }
    public record Acknowledgement(long appliedVersion) {}
    public record M5Decision(UUID driverId, String decision) {}
    public record M6TripAction(UUID driverId) {}
}
