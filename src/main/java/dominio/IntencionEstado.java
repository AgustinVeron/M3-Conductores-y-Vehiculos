package m3.dominio;

/**
 * Synchronisation status of an {@link DisponibilidadIntencion} with M4.
 *
 * <ul>
 *   <li>PENDING   – persisted in M3, not yet acknowledged by M4.</li>
 *   <li>RETRYING  – at least one HTTP attempt to M4 failed; will retry.</li>
 *   <li>APPLIED   – M4 acknowledged the desiredVersion.</li>
 *   <li>FAILED    – exhausted retries; requires manual recovery.</li>
 * </ul>
 */
public enum IntencionEstado {
    PENDING,
    RETRYING,
    APPLIED,
    FAILED
}
