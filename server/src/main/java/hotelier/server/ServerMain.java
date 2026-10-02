package hotelier.server;

import hotelier.config.ServerConfig;
import hotelier.util.JsonStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Avvio del server. Uso: {@code java -cp hotelier.jar hotelier.server.ServerMain [cartella-dati]}
 * (default: {@code resources}).
 */
public final class ServerMain {

    private ServerMain() {
    }

    public static void main(String[] args) {
        Path dir = Path.of(args.length > 0 ? args[0] : "resources");
        try {
            start(dir);
        } catch (Exception e) {
            System.err.println("[ERRORE] Impossibile avviare il server: " + e);
            System.exit(1);
        }
    }

    private static void start(Path dir) throws Exception {
        Path hotelsFile = dir.resolve("Hotels.json");
        if (!Files.exists(hotelsFile)) {
            throw new IOException("File degli hotel non trovato: " + hotelsFile.toAbsolutePath());
        }
        ServerConfig config = JsonStore.readOrCreate(dir.resolve("ServerConfig.json"), ServerConfig.class,
                ServerConfig.defaults());

        var hotels = new HotelRepository(hotelsFile);
        var users = new UserService(dir.resolve("Users.json"));
        var reviews = new ReviewService(dir.resolve("Reviews.json"), users, hotels);
        var services = new Services(users, hotels, reviews, new SessionRegistry());
        System.out.println("[OK] Dati caricati da " + dir.toAbsolutePath());

        var multicast = new MulticastNotifier(config.mcastAddress(), config.mcastPort());
        var rmi = new RmiServer(users, hotels, config.rmiPort(), config.rmiRemoteReference());
        var tcp = new NioServer(config.serverAddress(), config.tcpPort(), services);
        var ranking = new RankingService(hotels, reviews, multicast::notifyFirstPlace, rmi::notifyRanking);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdownNow();
            tcp.close();
            rmi.close();
            multicast.close();
        }, "shutdown"));

        tcp.start();
        scheduler.scheduleWithFixedDelay(ranking, 0, config.rankingInterval(), TimeUnit.SECONDS);
        System.out.println("[OK] HotelierServer in esecuzione: TCP " + config.tcpPort()
                + ", RMI " + config.rmiPort() + ", multicast " + config.mcastAddress() + ":" + config.mcastPort());
    }
}
