package m3.dominio.excepcion;

import java.util.UUID;

public class DocumentoNoEncontradoExcepcion extends RuntimeException {
    private final UUID documentId;

    public DocumentoNoEncontradoExcepcion(UUID documentId) {
        super("Documento not found or does not belong to the specified driver: " + documentId);
        this.documentId = documentId;
    }

    public UUID getDocumentId() {
        return documentId;
    }
}
