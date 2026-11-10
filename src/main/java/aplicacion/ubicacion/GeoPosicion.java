package m3.aplicacion.ubicacion;

/** Geographic point used by M3's replaceable location simulation adapter. */
public record GeoPosicion(double latitude, double longitude) {
    public GeoPosicion {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
    }
}
