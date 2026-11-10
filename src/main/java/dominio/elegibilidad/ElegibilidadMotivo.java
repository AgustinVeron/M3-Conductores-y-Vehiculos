package m3.dominio.elegibilidad;

public final class ElegibilidadMotivo {
    private ElegibilidadMotivo() {
        // Prevent instantiation
    }

    public static final String DRIVER_NOT_APPROVED = "DRIVER_NOT_APPROVED";
    public static final String DRIVER_SUSPENDED = "DRIVER_SUSPENDED";
    public static final String NO_VEHICLE_SELECTED = "NO_VEHICLE_SELECTED";
    public static final String VEHICLE_NOT_APPROVED = "VEHICLE_NOT_APPROVED";
    public static final String VEHICLE_SUSPENDED = "VEHICLE_SUSPENDED";
    public static final String LICENSE_MISSING = "LICENSE_MISSING";
    public static final String LICENSE_NOT_APPROVED = "LICENSE_NOT_APPROVED";
    public static final String LICENSE_EXPIRED = "LICENSE_EXPIRED";
    public static final String INSURANCE_MISSING = "INSURANCE_MISSING";
    public static final String INSURANCE_NOT_APPROVED = "INSURANCE_NOT_APPROVED";
    public static final String INSURANCE_EXPIRED = "INSURANCE_EXPIRED";
    public static final String SERVICE_TYPE_NOT_PERMITTED = "SERVICE_TYPE_NOT_PERMITTED";
}
