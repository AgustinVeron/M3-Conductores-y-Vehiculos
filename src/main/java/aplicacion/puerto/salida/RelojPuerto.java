package m3.aplicacion.puerto.salida;

import java.time.Instant;

/**
 * Puerto de salida para acceso al reloj del sistema.
 * Permite controlar el tiempo en pruebas de verificación de vigencias y marcas temporales.
 */
public interface RelojPuerto {

    Instant now();
}
