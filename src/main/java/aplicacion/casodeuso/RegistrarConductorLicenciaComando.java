package m3.aplicacion.casodeuso;

import m3.dominio.ServicioTipo;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RegistrarConductorLicenciaComando(UUID driverId, String documentNumber,
                                           Instant issuedAt, Instant validUntil,
                                           Set<ServicioTipo> serviceTypes,
                                           String storageReference, long expectedVersion,
                                           String idempotencyKey, String requestHash) {
    public RegistrarConductorLicenciaComando(UUID driverId, String documentNumber,
                                        Instant issuedAt, Instant validUntil,
                                        Set<ServicioTipo> serviceTypes,
                                        String storageReference, long expectedVersion) {
        this(driverId, documentNumber, issuedAt, validUntil, serviceTypes,
                storageReference, expectedVersion, null, null);
    }
}
