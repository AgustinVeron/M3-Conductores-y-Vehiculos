package m3.adaptador.api;

import m3.aplicacion.excepcion.ConductorAccesoDenegadoExcepcion;
import m3.aplicacion.excepcion.ConductorYaExisteExcepcion;
import m3.dominio.excepcion.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalApiExcepcionManejador {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @ExceptionHandler(ConductorYaExisteExcepcion.class)
    public ResponseEntity<ProblemDetail> handleDriverAlreadyExists(ConductorYaExisteExcepcion ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("https://utn.edu.ar/errors/driver-already-exists"));
        problem.setTitle("Conflicto de identidad de conductor");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "DRIVER_ALREADY_EXISTS");
        problem.setProperty("correlationId", correlationId);
        problem.setProperty("identitySubject", ex.getIdentitySubject());
        return buildResponse(HttpStatus.CONFLICT, problem, correlationId);
    }

    @ExceptionHandler(ConductorAccesoDenegadoExcepcion.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(ConductorAccesoDenegadoExcepcion ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setType(URI.create("urn:problem-type:access-denied"));
        problem.setTitle("Acceso denegado");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "ACCESS_DENIED");
        problem.setProperty("correlationId", correlationId);
        return buildResponse(HttpStatus.FORBIDDEN, problem, correlationId);
    }

    @ExceptionHandler(m3.aplicacion.excepcion.ConductorNoEncontradoExcepcion.class)
    public ResponseEntity<ProblemDetail> handleAppDriverNotFound(m3.aplicacion.excepcion.ConductorNoEncontradoExcepcion ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("urn:problem-type:driver-not-found"));
        problem.setTitle("Conductor no encontrado");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "DRIVER_NOT_FOUND");
        problem.setProperty("correlationId", correlationId);
        return buildResponse(HttpStatus.NOT_FOUND, problem, correlationId);
    }

    @ExceptionHandler(PrecondicionRequeridaExcepcion.class)
    public ResponseEntity<ProblemDetail> handlePreconditionRequired(PrecondicionRequeridaExcepcion ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_REQUIRED, ex.getMessage());
        problem.setType(URI.create("urn:m3:problem:precondition-required"));
        problem.setTitle("Precondition Required");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "PRECONDITION_REQUIRED");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.PRECONDITION_REQUIRED, problem, correlationId);
    }

    @ExceptionHandler(VersionInconsistenciaExcepcion.class)
    public ResponseEntity<ProblemDetail> handleVersionMismatch(VersionInconsistenciaExcepcion ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_FAILED, ex.getMessage());
        problem.setType(URI.create("urn:m3:problem:precondition-failed"));
        problem.setTitle("Precondition Failed");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "PRECONDITION_FAILED");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.PRECONDITION_FAILED, problem, correlationId);
    }

    // ── HU-10: Availability Intent exceptions ─────────────────────────────────

    @ExceptionHandler(m3.dominio.excepcion.ConductorNoEncontradoExcepcion.class)
    public ResponseEntity<ProblemDetail> handleDriverNotFound(
            m3.dominio.excepcion.ConductorNoEncontradoExcepcion ex,
            HttpServletRequest request
    ) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("urn:m3:problem:driver-not-found"));
        problem.setTitle("Conductor Not Found");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "DRIVER_NOT_FOUND");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.NOT_FOUND, problem, correlationId);
    }

    @ExceptionHandler(ConductorNoHabilitadoExcepcion.class)
    public ResponseEntity<ProblemDetail> handleDriverNotQualified(
            ConductorNoHabilitadoExcepcion ex,
            HttpServletRequest request
    ) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setType(URI.create("urn:m3:problem:driver-not-qualified"));
        problem.setTitle("Conductor Not Qualified");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "DRIVER_NOT_QUALIFIED");
        problem.setProperty("reason", ex.getReason());
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, problem, correlationId);
    }

    @ExceptionHandler(IdempotenciaConflictoExcepcion.class)
    public ResponseEntity<ProblemDetail> handleIdempotencyConflict(
            IdempotenciaConflictoExcepcion ex,
            HttpServletRequest request
    ) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("correlationId", correlationId);

        if (request.getRequestURI() != null && request.getRequestURI().contains("availability-intent")) {
            problem.setType(URI.create("urn:m3:problem:idempotency-conflict"));
            problem.setTitle("Idempotency Key Conflict");
            problem.setProperty("code", "IDEMPOTENCY_CONFLICT");
        } else {
            problem.setType(URI.create("urn:m3:problem:conflict"));
            problem.setTitle("Idempotency Conflict");
            problem.setProperty("code", "IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD");
        }

        return buildResponse(HttpStatus.CONFLICT, problem, correlationId);
    }

    // ── Existing handlers ─────────────────────────────────────────────────────

    @ExceptionHandler(VehiculoNoEncontradoExcepcion.class)
    public ResponseEntity<ProblemDetail> handleVehicleNotFound(VehiculoNoEncontradoExcepcion ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("urn:m3:problem:not-found"));
        problem.setTitle("Vehiculo Not Found");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "VEHICLE_NOT_FOUND");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.NOT_FOUND, problem, correlationId);
    }

    @ExceptionHandler(VehiculoProhibidoExcepcion.class)
    public ResponseEntity<ProblemDetail> handleVehicleForbidden(VehiculoProhibidoExcepcion ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setType(URI.create("urn:m3:problem:forbidden"));
        problem.setTitle("Vehiculo Access Forbidden");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "VEHICLE_FORBIDDEN");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.FORBIDDEN, problem, correlationId);
    }

    @ExceptionHandler(DocumentoNoEncontradoExcepcion.class)
    public ResponseEntity<ProblemDetail> handleDocumentNotFound(DocumentoNoEncontradoExcepcion ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("urn:m3:problem:not-found"));
        problem.setTitle("Documento Not Found");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "RESOURCE_NOT_FOUND");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.NOT_FOUND, problem, correlationId);
    }

    @ExceptionHandler(InvalidoDocumentoPeriodoExcepcion.class)
    public ResponseEntity<ProblemDetail> handleInvalidPeriod(InvalidoDocumentoPeriodoExcepcion ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setType(URI.create("urn:m3:problem:invalid-period"));
        problem.setTitle("Invalid Documento Period");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "INVALID_DOCUMENT_PERIOD");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, problem, correlationId);
    }

    @ExceptionHandler(DominioValidacionExcepcion.class)
    public ResponseEntity<ProblemDetail> handleDomainValidation(DominioValidacionExcepcion ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("urn:m3:problem:bad-request"));
        problem.setTitle("Bad Request");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "DOMAIN_VALIDATION_ERROR");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.BAD_REQUEST, problem, correlationId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        boolean isDriverEndpoint = request.getRequestURI() != null && request.getRequestURI().endsWith("/api/v1/drivers");
        String code = isDriverEndpoint ? "VALIDATION_FAILED" : "VALIDATION_ERROR";
        String title = isDriverEndpoint ? "Error de validación en la solicitud" : "Validation Error";

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                isDriverEndpoint ? "La solicitud contiene datos inválidos o incompletos" : "Request validation failed");
        problem.setType(URI.create(isDriverEndpoint ? "https://utn.edu.ar/errors/validation-error" : "urn:m3:problem:validation"));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code);
        problem.setProperty("correlationId", correlationId);

        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> Map.of(
                        "field", err.getField(),
                        "message", err.getDefaultMessage() != null ? err.getDefaultMessage() : "invalid value"
                ))
                .toList();
        problem.setProperty("errors", errors);

        Map<String, String> invalidFields = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            invalidFields.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("invalidFields", invalidFields);

        return buildResponse(HttpStatus.BAD_REQUEST, problem, correlationId);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        String detail = String.format("El parámetro '%s' con valor '%s' no tiene el formato válido esperado.",
                ex.getName(), ex.getValue());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setType(URI.create("urn:problem-type:bad-request"));
        problem.setTitle("Parámetro inválido");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "INVALID_PARAMETER_FORMAT");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.BAD_REQUEST, problem, correlationId);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Malformed JSON request body: " + ex.getMostSpecificCause().getMessage());
        problem.setType(URI.create("urn:m3:problem:malformed-request"));
        problem.setTitle("Malformed JSON Request");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "MALFORMED_REQUEST");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.BAD_REQUEST, problem, correlationId);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        if ("If-Match".equalsIgnoreCase(ex.getHeaderName())) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_REQUIRED,
                    "If-Match header is required for state mutating operations");
            problem.setType(URI.create("urn:m3:problem:precondition-required"));
            problem.setTitle("Precondition Required");
            problem.setInstance(URI.create(request.getRequestURI()));
            problem.setProperty("code", "PRECONDITION_REQUIRED");
            problem.setProperty("correlationId", correlationId);
            return buildResponse(HttpStatus.PRECONDITION_REQUIRED, problem, correlationId);
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Missing required header: " + ex.getHeaderName());
        problem.setType(URI.create("urn:m3:problem:bad-request"));
        problem.setTitle("Bad Request");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "MISSING_HEADER");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.BAD_REQUEST, problem, correlationId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("urn:m3:problem:bad-request"));
        problem.setTitle("Bad Request");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "BAD_REQUEST");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.BAD_REQUEST, problem, correlationId);
    }

    @ExceptionHandler(IntegracionNoDisponibleExcepcion.class)
    public ResponseEntity<ProblemDetail> integrationUnavailable(IntegracionNoDisponibleExcepcion ex,HttpServletRequest request) {
        var problem=ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,ex.getMessage());
        problem.setTitle("Integración no disponible"); problem.setInstance(URI.create(request.getRequestURI()));
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE,problem,resolveCorrelationId(request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred");
        problem.setType(URI.create("urn:m3:problem:internal-server-error"));
        problem.setTitle("Internal Server Error");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "INTERNAL_SERVER_ERROR");
        problem.setProperty("correlationId", correlationId);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, problem, correlationId);
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        Object attr = request.getAttribute(CORRELATION_ID_HEADER);
        if (attr instanceof String s && !s.isBlank()) return s;
        String header = request.getHeader(CORRELATION_ID_HEADER);
        if (header != null && !header.isBlank()) return header;
        String generated = UUID.randomUUID().toString();
        request.setAttribute(CORRELATION_ID_HEADER, generated);
        return generated;
    }

    private ResponseEntity<ProblemDetail> buildResponse(HttpStatus status, ProblemDetail problem, String correlationId) {
        return ResponseEntity.status(status)
                .header(CORRELATION_ID_HEADER, correlationId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .body(problem);
    }
}