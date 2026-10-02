package hotelier.client;

import hotelier.model.CityRanking;
import hotelier.rmi.ClientCallback;
import hotelier.rmi.ServerService;
import hotelier.util.Result;

import java.io.Closeable;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/** Lato client di RMI: registrazione utenti e ricezione delle variazioni delle classifiche locali. */
final class RmiClient implements Closeable {

    private final ServerService server;
    private final RankingCache cache = new RankingCache();
    private final ClientCallback callbackStub;
    private List<String> interests = List.of();

    RmiClient(String host, int port, String serviceName) throws Exception {
        server = (ServerService) LocateRegistry.getRegistry(host, port).lookup(serviceName);
        callbackStub = (ClientCallback) UnicastRemoteObject.exportObject(cache, 0);
    }

    Result register(String username, String password) throws RemoteException {
        return server.register(username, password);
    }

    /** Sostituisce le città di interesse; il server invia subito la classifica corrente di ciascuna. */
    synchronized void setInterests(List<String> cities) throws RemoteException {
        cache.clear();
        interests = List.copyOf(cities);
        server.registerCallback(callbackStub, interests);
    }

    synchronized void clearInterests() throws RemoteException {
        boolean registered = !interests.isEmpty();
        interests = List.of();
        cache.clear();
        if (registered) {
            server.unregisterCallback(callbackStub);
        }
    }

    synchronized List<String> interests() {
        return interests;
    }

    List<CityRanking> rankings() {
        return cache.snapshot();
    }

    void addRankingListener(Runnable listener) {
        cache.addListener(listener);
    }

    @Override
    public void close() {
        try {
            clearInterests();
        } catch (RemoteException e) {
            // server non più raggiungibile
        }
        try {
            UnicastRemoteObject.unexportObject(cache, true);
        } catch (NoSuchObjectException e) {
            // già rimosso
        }
    }
}
