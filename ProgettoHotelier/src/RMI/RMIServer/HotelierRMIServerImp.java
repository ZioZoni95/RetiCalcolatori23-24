package RMI.RMIServer;

import RMI.RMIClient.HotelierClientInterface;
import server.HotelierServerAuthUserHandler;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotelierRMIServerImp implements HotelierServerInterface {

    private final Map<HotelierClientInterface, List<String>> clientCallback;
    private HotelierServerAuthUserHandler usersAuthenticated;

    public HotelierRMIServerImp(){
        usersAuthenticated = HotelierServerAuthUserHandler.getInstance();
        clientCallback = new HashMap<>();
    }

    // registra callbacks per le città di interesse dell' interfaccia rmi client passato
    @Override
    public synchronized void registerCallback(HotelierClientInterface callbackClient, List<String> cities) throws RemoteException {
        // aggiunge stub client e relativa lista delle città di interesse alla mappa
        clientCallback.put(callbackClient, cities);
    }

    @Override
    public String registerUser(String username, String password) throws RemoteException {
        return usersAuthenticated.register(username, password);
    }

    // deregistra callbacks per interfaccia rmi client passato
    @Override
    public synchronized void unregisterCallback(HotelierClientInterface callbackClient) throws RemoteException {
        // rimuove stub client dalla mappa
        clientCallback.remove(callbackClient);
    }

    // restituisce mappa delle callbacks
    public synchronized Map<HotelierClientInterface, List<String>> getClientsCallback() {
        return clientCallback;
    }
}
