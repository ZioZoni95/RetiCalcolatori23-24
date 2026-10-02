package hotelier.server;

import hotelier.model.CityRanking;
import hotelier.rmi.ClientCallback;
import hotelier.rmi.ServerService;
import hotelier.util.Result;

import java.io.Closeable;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** Server RMI: registrazione utenti e callback per le classifiche locali. */
public final class RmiServer implements ServerService, Closeable {

    private static final System.Logger LOG = System.getLogger(RmiServer.class.getName());

    private final UserService users;
    private final HotelRepository hotels;
    private final EventLog events;
    private final Map<ClientCallback, Set<String>> callbacks = new ConcurrentHashMap<>();
    private final String name;
    private final Registry registry;

    public RmiServer(UserService users, HotelRepository hotels, EventLog events, int port, String name)
            throws RemoteException {
        this.users = users;
        this.hotels = hotels;
        this.events = events;
        this.name = name;
        ServerService stub = (ServerService) UnicastRemoteObject.exportObject(this, 0);
        this.registry = LocateRegistry.createRegistry(port);
        registry.rebind(name, stub);
    }

    @Override
    public Result register(String username, String password) {
        Result result = users.register(username, password);
        if (result.ok()) {
            events.log("Registrazione (RMI): " + username);
        }
        return result;
    }

    @Override
    public void registerCallback(ClientCallback callback, List<String> cities) throws RemoteException {
        events.log("Callback RMI registrata per: " + String.join(", ", cities));
        callbacks.put(callback, cities.stream()
                .map(c -> c.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet()));
        try {
            for (String city : cities) {
                var ranking = hotels.cityRanking(city);
                if (ranking.isPresent()) {
                    callback.rankingChanged(ranking.get());
                }
            }
        } catch (RemoteException e) {
            callbacks.remove(callback);
            throw e;
        }
    }

    @Override
    public void unregisterCallback(ClientCallback callback) {
        callbacks.remove(callback);
    }

    /** Notifica i client interessati alla città; quelli irraggiungibili vengono rimossi. */
    public void notifyRanking(CityRanking ranking) {
        String city = ranking.city().toLowerCase(Locale.ROOT);
        callbacks.forEach((callback, cities) -> {
            if (!cities.contains(city)) {
                return;
            }
            try {
                callback.rankingChanged(ranking);
            } catch (RemoteException e) {
                LOG.log(System.Logger.Level.INFO, "Callback non raggiungibile, la rimuovo");
                callbacks.remove(callback);
            }
        });
    }

    @Override
    public void close() {
        try {
            registry.unbind(name);
        } catch (RemoteException | NotBoundException e) {
            // già rimosso: niente da fare
        }
        try {
            UnicastRemoteObject.unexportObject(this, true);
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (NoSuchObjectException e) {
            // già rimosso: niente da fare
        }
    }
}
