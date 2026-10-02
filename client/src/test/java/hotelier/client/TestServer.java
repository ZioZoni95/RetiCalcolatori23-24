package hotelier.client;

import hotelier.config.ClientConfig;
import hotelier.config.ServerConfig;
import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.server.ServerRuntime;
import hotelier.util.JsonStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.List;

/** Un server reale su porte libere di loopback, con tre hotel e nessuna recensione. */
public final class TestServer implements AutoCloseable {

    static {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
    }

    public final ServerRuntime runtime;
    public final ClientConfig clientConfig;

    public static TestServer start(Path dir) throws Exception {
        JsonStore.write(dir.resolve("Hotels.json"), List.of(
                hotel(1, "Hotel Roma 1", "Roma"),
                hotel(2, "Hotel Roma 2", "Roma"),
                hotel(3, "Hotel Milano 1", "Milano")));
        ServerConfig config = new ServerConfig(freePort(), freePort(), freePort(), 3600, "127.0.0.1",
                "Hotelier-Test", "230.0.0.1");
        JsonStore.write(dir.resolve("ServerConfig.json"), config);
        return new TestServer(ServerRuntime.start(dir),
                new ClientConfig(config.tcpPort(), config.rmiPort(), config.mcastPort(), "127.0.0.1",
                        config.rmiRemoteReference(), config.mcastAddress()));
    }

    private TestServer(ServerRuntime runtime, ClientConfig clientConfig) {
        this.runtime = runtime;
        this.clientConfig = clientConfig;
    }

    public HotelierClient connect() throws Exception {
        return HotelierClient.connect(clientConfig);
    }

    private static Hotel hotel(int id, String name, String city) {
        return new Hotel(id, name, "descrizione", city, "000", List.of("TV"), 0, Ratings.ZERO, 0, 0, 0);
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Override
    public void close() {
        runtime.close();
    }
}
