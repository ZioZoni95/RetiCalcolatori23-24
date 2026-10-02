package hotelier.server;

import hotelier.config.ServerConfig;
import hotelier.model.Hotel;
import hotelier.model.User;
import hotelier.util.JsonStore;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Un server Hotelier in esecuzione: carica i dati, avvia TCP, RMI, multicast e ranking periodico.
 * È il nucleo condiviso dalle interfacce (headless, TUI, GUI), che lo osservano tramite
 * {@link #events()} e i metodi di statistica.
 */
public final class ServerRuntime implements Closeable {

    private final Path dataDir;
    private final ServerConfig config;
    private final Services services;
    private final MulticastNotifier multicast;
    private final RmiServer rmi;
    private final NioServer tcp;
    private final RankingService ranking;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "ranking");
        thread.setDaemon(true);
        return thread;
    });
    private final Instant startedAt = Instant.now();
    private boolean closed;

    /** Legge la configurazione da {@code ServerConfig.json} (creandola se manca). */
    public static ServerRuntime start(Path dataDir) throws Exception {
        ServerConfig config = JsonStore.readOrCreate(dataDir.resolve("ServerConfig.json"), ServerConfig.class,
                ServerConfig.defaults());
        return start(dataDir, config);
    }

    public static ServerRuntime start(Path dataDir, ServerConfig config) throws Exception {
        return new ServerRuntime(dataDir, config);
    }

    private ServerRuntime(Path dataDir, ServerConfig config) throws Exception {
        this.dataDir = dataDir;
        this.config = config;

        Path hotelsFile = dataDir.resolve("Hotels.json");
        if (!Files.exists(hotelsFile)) {
            throw new IOException("File degli hotel non trovato: " + hotelsFile.toAbsolutePath());
        }
        var hotels = new HotelRepository(hotelsFile);
        var users = new UserService(dataDir.resolve("Users.json"));
        var reviews = new ReviewService(dataDir.resolve("Reviews.json"), users, hotels);
        EventLog events = new EventLog();
        this.services = new Services(users, hotels, reviews, new SessionRegistry(), events);
        events.log("Dati caricati da " + dataDir.toAbsolutePath());

        MulticastNotifier multicastNotifier = null;
        RmiServer rmiServer = null;
        NioServer tcpServer = null;
        try {
            multicastNotifier = new MulticastNotifier(config.mcastAddress(), config.mcastPort());
            rmiServer = new RmiServer(users, hotels, events, config.rmiPort(), config.rmiRemoteReference());
            tcpServer = new NioServer(config.serverAddress(), config.tcpPort(), services);
        } catch (Exception e) {
            closeQuietly(tcpServer, rmiServer, multicastNotifier);
            throw e;
        }
        this.multicast = multicastNotifier;
        this.rmi = rmiServer;
        this.tcp = tcpServer;

        this.ranking = new RankingService(hotels, reviews,
                hotel -> {
                    events.log("Nuovo primo hotel a " + hotel.city() + ": " + hotel.name());
                    multicast.notifyFirstPlace(hotel);
                },
                cityRanking -> {
                    events.log("Classifica di " + cityRanking.city() + " cambiata");
                    rmi.notifyRanking(cityRanking);
                });

        tcp.start();
        scheduler.scheduleWithFixedDelay(ranking, 0, config.rankingInterval(), TimeUnit.SECONDS);
        events.log("Server in esecuzione: TCP " + config.tcpPort() + ", RMI " + config.rmiPort()
                + ", multicast " + config.mcastAddress() + ":" + config.mcastPort());
    }

    public ServerConfig config() {
        return config;
    }

    public Path dataDir() {
        return dataDir;
    }

    public EventLog events() {
        return services.events();
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Duration uptime() {
        return Duration.between(startedAt, Instant.now());
    }

    public int connectedClients() {
        return tcp.connectionCount();
    }

    public List<String> loggedInUsers() {
        return services.sessions().users();
    }

    public List<User> users() {
        return services.users().all();
    }

    public List<Hotel> hotels() {
        return services.hotels().all();
    }

    public int reviewCount() {
        return services.reviews().count();
    }

    /** Richiede un ricalcolo immediato del ranking (eseguito in background). */
    public void rankNow() {
        if (!scheduler.isShutdown()) {
            scheduler.execute(ranking);
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        services.events().log("Server fermato");
        // il ranking in corso termina (e salva) prima di chiudere il resto
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        closeQuietly(tcp, rmi, multicast);
    }

    private static void closeQuietly(Closeable... resources) {
        for (Closeable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (IOException | RuntimeException e) {
                    // in chiusura
                }
            }
        }
    }
}
