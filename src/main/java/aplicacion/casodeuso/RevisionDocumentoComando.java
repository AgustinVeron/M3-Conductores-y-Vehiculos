package m3.aplicacion.casodeuso;

import m3.dominio.RevisionDecision;
import java.util.UUID;

public record RevisionDocumentoComando(
        UUID driverId,
        UUID documentId,
        RevisionDecision decision,
        String reason,
        long ifMatchVersion,
        String actor,
        String correlationId
) {}
