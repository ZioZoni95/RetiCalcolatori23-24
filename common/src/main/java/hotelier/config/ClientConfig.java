package hotelier.config;

/** Configurazione del client (file {@code ClientConfig.json}). */
public record ClientConfig(
        int tcpPort,
        int rmiPort,
        int mcastPort,
        String serverAddress,
        String rmiRemoteReference,
        String mcastAddress) {

    public static ClientConfig defaults() {
        return new ClientConfig(9999, 1099, 49152, "localhost", "Hotelier-Service-Program", "230.0.0.0");
    }
}
