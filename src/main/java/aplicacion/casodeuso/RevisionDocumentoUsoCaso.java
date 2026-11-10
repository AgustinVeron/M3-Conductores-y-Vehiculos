package m3.aplicacion.casodeuso;

import m3.dominio.Documento;

public interface RevisionDocumentoUsoCaso {
    Documento reviewDocument(RevisionDocumentoComando command);
}
