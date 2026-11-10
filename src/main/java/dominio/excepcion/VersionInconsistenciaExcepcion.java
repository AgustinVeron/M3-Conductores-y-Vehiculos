package m3.dominio.excepcion;

public class VersionInconsistenciaExcepcion extends RuntimeException {
    private final long currentVersion;
    private final long expectedVersion;

    public VersionInconsistenciaExcepcion(long currentVersion, long expectedVersion) {
        super(String.format("If-Match version mismatch: expected %d, but current version is %d",
                expectedVersion, currentVersion));
        this.currentVersion = currentVersion;
        this.expectedVersion = expectedVersion;
    }

    public long getCurrentVersion() {
        return currentVersion;
    }

    public long getExpectedVersion() {
        return expectedVersion;
    }
}
