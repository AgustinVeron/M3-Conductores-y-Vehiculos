package m3.aplicacion.puerto;
import m3.dominio.EntregaResultado;
import m3.dominio.DisponibilidadMensaje;
public interface M4DisponibilidadPuerto {
    EntregaResultado send(DisponibilidadMensaje message);
}
