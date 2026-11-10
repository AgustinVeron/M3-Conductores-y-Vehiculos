package m3.adaptador.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Controlador de comprobación de salud conforme a las especificaciones contractuales de M3:
 * - /health/live: Liveness probe (proceso vivo)
 * - /health/ready: Readiness probe (dependencias indispensables, ej. base de datos)
 */
@RestController
public class SaludApiControlador {

    private final DataSource dataSource;

    public SaludApiControlador(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health/live")
    public ResponseEntity<Map<String, String>> liveness() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                return ResponseEntity.ok(Map.of("status", "UP", "database", "CONNECTED"));
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "DOWN", "database", "INVALID_CONNECTION"));
        } catch (SQLException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "DOWN", "database", "UNREACHABLE", "error", ex.getMessage()));
        }
    }
}
