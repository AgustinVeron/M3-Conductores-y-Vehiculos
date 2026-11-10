package m3.dominio;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class IdempotenciaRegistro {
    private final UUID id;
    private final String idempotencyKey;
    private final String principal;
    private final String httpMethod;
    private final String requestPath;
    private final String requestHash;
    private final int responseStatus;
    private final String responseBody;
    private final Instant createdAt;

    public IdempotenciaRegistro(UUID id, String idempotencyKey, String principal,
                             String httpMethod, String requestPath, String requestHash,
                             int responseStatus, String responseBody, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Idempotency record ID must not be null");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "Idempotency key must not be null");
        this.principal = principal != null ? principal : "anonymous";
        this.httpMethod = Objects.requireNonNull(httpMethod, "HTTP method must not be null");
        this.requestPath = Objects.requireNonNull(requestPath, "Request path must not be null");
        this.requestHash = Objects.requireNonNull(requestHash, "Request hash must not be null");
        this.responseStatus = responseStatus;
        this.responseBody = responseBody != null ? responseBody : "";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public IdempotenciaRegistro(String principal, String httpMethod, String requestPath,
                             String idempotencyKey, String requestHash, int responseStatus,
                             String responseBody) {
        this(UUID.randomUUID(), idempotencyKey, principal, httpMethod, requestPath, requestHash,
                responseStatus, responseBody != null ? responseBody : "", Instant.now());
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getPrincipal() { return principal; }
    public String getHttpMethod() { return httpMethod; }
    public String getRequestPath() { return requestPath; }
    public String getRequestHash() { return requestHash; }
    public String requestHash() { return requestHash; }
    public int getResponseStatus() { return responseStatus; }
    public String getResponseBody() { return responseBody; }
    public Instant getCreatedAt() { return createdAt; }
}
