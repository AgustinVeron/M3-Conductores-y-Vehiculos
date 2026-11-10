package m3.aplicacion.puerto;

import m3.aplicacion.ubicacion.UbicacionActualizacion;

/** Output port used by the isolated location simulator to publish positions to M4. */
public interface M4UbicacionPuerto {
    void sendLocation(UbicacionActualizacion update);
}
