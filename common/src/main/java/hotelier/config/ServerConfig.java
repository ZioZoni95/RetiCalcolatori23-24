package hotelier.config;

/**
 * Configurazione del server (file {@code ServerConfig.json}).
 *
 * @param rankingInterval secondi tra due ricalcoli del ranking
 */
public record ServerConfig(
        int tcpPort,
        int rmiPort,
        int mcastPort,
        int rankingInterval,
        String serverAddress,
        String rmiRemoteReference,
        String mcastAddress) {

    public static ServerConfig defaults() {
        return new ServerConfig(9999, 1099, 49152, 10, "localhost", "Hotelier-Service-Program", "230.0.0.0");
    }
}
