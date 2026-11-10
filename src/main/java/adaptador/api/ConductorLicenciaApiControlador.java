package m3.adaptador.api;

import m3.adaptador.api.dto.ConductorLicenciaRespuesta;
import m3.adaptador.api.dto.EnviarConductorLicenciaSolicitud;
import m3.aplicacion.casodeuso.RegistrarConductorLicenciaComando;
import m3.aplicacion.casodeuso.RegistrarConductorLicenciaUsoCaso;
import m3.dominio.Documento;
import m3.dominio.excepcion.PrecondicionRequeridaExcepcion;
import jakarta.validation.Valid;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drivers/{driverId}/documents")
public class ConductorLicenciaApiControlador {
    private final RegistrarConductorLicenciaUsoCaso licenses;

    public ConductorLicenciaApiControlador(RegistrarConductorLicenciaUsoCaso licenses) {
        this.licenses = licenses;
    }

    @GetMapping
    public ResponseEntity<DriverLicensePage> history(@PathVariable UUID driverId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") String limit) {
        if (!limit.matches("[1-9][0-9]*") || (cursor != null && !cursor.matches("[1-9][0-9]*"))) {
            throw new IllegalArgumentException("limit must be 1..100 and cursor must be a positive revision");
        }
        int pageSize = Integer.parseInt(limit);
        Integer beforeRevision = cursor == null ? null : Integer.parseInt(cursor);
        if (pageSize > 100) {
            throw new IllegalArgumentException("limit must be 1..100 and cursor must be a positive revision");
        }
        List<Documento> documents = licenses.history(driverId).stream()
                .filter(item -> beforeRevision == null || item.getRevision() < beforeRevision)
                .limit(pageSize + 1L).toList();
        List<ConductorLicenciaRespuesta> items = documents.stream().limit(pageSize)
                .map(ConductorLicenciaRespuesta::from).toList();
        String nextCursor = documents.size() > pageSize
                ? String.valueOf(documents.get(pageSize - 1).getRevision()) : null;
        long version = licenses.driver(driverId).getVersion();
        return ResponseEntity.ok().eTag("\"" + version + "\"")
                .header("X-Correlation-ID", correlationId(correlationId))
                .body(new DriverLicensePage(items, nextCursor));
    }

    @PostMapping
    public ResponseEntity<ConductorLicenciaRespuesta> submit(@PathVariable UUID driverId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            @Valid @RequestBody EnviarConductorLicenciaSolicitud request) {
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new PrecondicionRequeridaExcepcion("If-Match header is required");
        }
        String value = ifMatch.replaceAll("^\"|\"$", "");
        if (!value.matches("[1-9][0-9]*")) {
            throw new IllegalArgumentException("If-Match must be a positive driver version");
        }
        if (idempotencyKey == null || idempotencyKey.length() < 16 || idempotencyKey.length() > 128) {
            throw new IllegalArgumentException("Idempotency-Key must be between 16 and 128 characters");
        }
        long expectedVersion = Long.parseLong(value);
        String canonical = expectedVersion + "|" + request.documentNumber() + "|" + request.issuedAt()
                + "|" + request.validUntil() + "|" + request.serviceTypes().stream().sorted().toList()
                + "|" + request.storageReference();
        Documento created = licenses.register(new RegistrarConductorLicenciaComando(driverId,
                request.documentNumber(), request.issuedAt(), request.validUntil(),
                request.serviceTypes(), request.storageReference(), expectedVersion,
                idempotencyKey, sha256(canonical)));
        return ResponseEntity.created(URI.create("/api/v1/drivers/" + driverId + "/documents/" + created.getId()))
                .eTag("\"" + created.getDriverVersion() + "\"")
                .header("X-Correlation-ID", correlationId(correlationId))
                .body(ConductorLicenciaRespuesta.from(created));
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private String correlationId(String supplied) {
        return supplied == null || supplied.isBlank() ? UUID.randomUUID().toString() : supplied;
    }

    public record DriverLicensePage(List<ConductorLicenciaRespuesta> items, String nextCursor) {}
}
