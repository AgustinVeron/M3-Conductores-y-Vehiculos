package m3.adaptador.api;

import m3.adaptador.api.dto.DocumentoRespuesta;
import m3.adaptador.api.dto.RevisionDocumentoSolicitud;
import m3.aplicacion.puerto.IdempotenciaRepositorio;
import m3.aplicacion.casodeuso.RevisionDocumentoComando;
import m3.aplicacion.casodeuso.RevisionDocumentoUsoCaso;
import m3.dominio.Documento;
import m3.dominio.IdempotenciaRegistro;
import m3.dominio.excepcion.IdempotenciaConflictoExcepcion;
import m3.dominio.excepcion.PrecondicionRequeridaExcepcion;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers/{driverId}/documents/{documentId}/reviews")
@Tag(name = "Documents", description = "Operaciones de documentación y revisión")
public class DocumentoRevisionControlador {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private final RevisionDocumentoUsoCaso reviewDocumentUseCase;
    private final IdempotenciaRepositorio idempotencyRepository;
    private final ObjectMapper objectMapper;

    public DocumentoRevisionControlador(RevisionDocumentoUsoCaso reviewDocumentUseCase,
                                    IdempotenciaRepositorio idempotencyRepository,
                                    ObjectMapper objectMapper) {
        this.reviewDocumentUseCase = reviewDocumentUseCase;
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> reviewDocument(
            @PathVariable UUID driverId,
            @PathVariable UUID documentId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatchHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = CORRELATION_ID_HEADER, required = false) String correlationIdHeader,
            @Valid @RequestBody RevisionDocumentoSolicitud request,
            HttpServletRequest servletRequest) throws JsonProcessingException {

        String correlationId = (correlationIdHeader != null && !correlationIdHeader.isBlank())
                ? correlationIdHeader
                : UUID.randomUUID().toString();
        servletRequest.setAttribute(CORRELATION_ID_HEADER, correlationId);

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }
        if (idempotencyKey.length() < 16 || idempotencyKey.length() > 128) {
            throw new IllegalArgumentException("Idempotency-Key must be between 16 and 128 characters");
        }

        if (ifMatchHeader == null || ifMatchHeader.isBlank()) {
            throw new PrecondicionRequeridaExcepcion("If-Match header is required for reviewing documents");
        }
        String sanitizedIfMatch = ifMatchHeader.replaceAll("^\"|\"$", "").trim();
        if (!sanitizedIfMatch.matches("^[1-9][0-9]*$")) {
            throw new IllegalArgumentException("If-Match header must match format ^[1-9][0-9]*$");
        }
        long ifMatchVersion = Long.parseLong(sanitizedIfMatch);

        String requestPath = servletRequest.getRequestURI();
        String requestPayload = request.decision().name() + ":" + request.reason();
        String requestHash = computeSha256(requestPayload);

        Optional<IdempotenciaRegistro> existingRecord = idempotencyRepository
                .findByKeyAndMethodAndPath(idempotencyKey, "POST", requestPath);

        if (existingRecord.isPresent()) {
            IdempotenciaRegistro record = existingRecord.get();
            if (!record.getRequestHash().equals(requestHash)) {
                throw new IdempotenciaConflictoExcepcion("Idempotency-Key was already used with a different request payload");
            }
            DocumentoRespuesta cachedResponse = objectMapper.readValue(record.getResponseBody(), DocumentoRespuesta.class);
            return ResponseEntity.status(record.getResponseStatus())
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .header(HttpHeaders.ETAG, "\"" + cachedResponse.driverVersion() + "\"")
                    .body(cachedResponse);
        }

        String actor = java.util.Objects.toString(servletRequest.getAttribute("m3.actor"), "LOCAL_OPERATOR");
        RevisionDocumentoComando command = new RevisionDocumentoComando(
                driverId,
                documentId,
                request.decision(),
                request.reason(),
                ifMatchVersion,
                actor,
                correlationId
        );

        Documento updatedDocument = reviewDocumentUseCase.reviewDocument(command);
        DocumentoRespuesta responseBody = DocumentoRespuesta.fromDomain(updatedDocument);

        String serializedBody = objectMapper.writeValueAsString(responseBody);
        IdempotenciaRegistro idempotencyRecord = new IdempotenciaRegistro(
                UUID.randomUUID(),
                idempotencyKey,
                actor,
                "POST",
                requestPath,
                requestHash,
                HttpStatus.OK.value(),
                serializedBody,
                Instant.now()
        );
        idempotencyRepository.save(idempotencyRecord);

        return ResponseEntity.ok()
                .header(CORRELATION_ID_HEADER, correlationId)
                .header(HttpHeaders.ETAG, "\"" + updatedDocument.getDriverVersion() + "\"")
                .body(responseBody);
    }

    private String computeSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
