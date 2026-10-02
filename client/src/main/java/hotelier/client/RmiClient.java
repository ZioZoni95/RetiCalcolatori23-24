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
    private boolean interested;

    RmiClient(String host, int port, String serviceName) throws Exception {
        server = (ServerService) LocateRegistry.getRegistry(host, port).lookup(serviceName);
        callbackStub = (ClientCallback) UnicastRemoteObject.exportObject(cache, 0);
    }

    Result register(String username, String password) throws RemoteException {
        return server.register(username, password);
    }

    synchronized void registerInterests(List<String> cities) throws RemoteException {
        server.registerCallback(callbackStub, cities);
        interested = true;
    }

    synchronized void unregisterInterests() throws RemoteException {
        if (interested) {
            interested = false;
            server.unregisterCallback(callbackStub);
        }
        cache.clear();
    }

    List<CityRanking> rankings() {
        return cache.snapshot();
    }

    @Override
    public void close() {
        try {
            unregisterInterests();
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
