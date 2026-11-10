package m3.adaptador.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_idempotency", columnNames = {"idempotency_key", "http_method", "request_path"})
})
public class IdempotenciaRegistroJpaEntidad {

    @Id
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "principal", nullable = false, length = 100)
    private String principal;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "request_path", nullable = false, length = 255)
    private String requestPath;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_status", nullable = false)
    private Integer responseStatus;

    @Column(name = "response_body", nullable = false, columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public IdempotenciaRegistroJpaEntidad() {
    }

    public IdempotenciaRegistroJpaEntidad(UUID id, String idempotencyKey, String principal,
                                      String httpMethod, String requestPath, String requestHash,
                                      Integer responseStatus, String responseBody, Instant createdAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.principal = principal;
        this.httpMethod = httpMethod;
        this.requestPath = requestPath;
        this.requestHash = requestHash;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
