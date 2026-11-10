package m3.aplicacion.casodeuso;
import m3.aplicacion.puerto.ConductorRepositorio;
import m3.aplicacion.puerto.salida.ActualPrincipalPuerto;
import m3.aplicacion.excepcion.ConductorAccesoDenegadoExcepcion;
import m3.dominio.Conductor;
import m3.dominio.excepcion.ConductorNoEncontradoExcepcion;
import java.util.UUID;
public class ConductorAccesoServicio {
    private final ConductorRepositorio drivers;
    private final ActualPrincipalPuerto principal;
    public ConductorAccesoServicio(ConductorRepositorio drivers, ActualPrincipalPuerto principal) { this.drivers=drivers; this.principal=principal; }
    public String actor() { return principal.getSubject().orElseThrow(() -> new ConductorAccesoDenegadoExcepcion("Se requiere una identidad autenticada.")); }
    public boolean operator() { return principal.hasRole("OPERATOR") || principal.hasRole("OPERADOR") || principal.hasRole("ADMIN") || principal.hasRole("ADMINISTRADOR"); }
    public void requireOperator() { actor(); if (!operator()) throw new ConductorAccesoDenegadoExcepcion("Se requiere rol de operador o administrador."); }
    public Conductor driver(UUID id) {
        String subject=actor();
        Conductor d=drivers.findById(id).orElseThrow(() -> new ConductorNoEncontradoExcepcion(id));
        if (!operator() && !d.isOwnedBy(subject)) throw new ConductorAccesoDenegadoExcepcion("No tenés permiso para acceder a este conductor.");
        return d;
    }
    public void own(UUID id) {
        Conductor d=driver(id);
        if(!d.isOwnedBy(actor())) throw new ConductorAccesoDenegadoExcepcion("La operación corresponde al propio conductor.");
    }
}
