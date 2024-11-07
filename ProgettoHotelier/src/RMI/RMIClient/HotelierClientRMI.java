package RMI.RMIClient;

import RMI.RMIServer.HotelierServerInterface;
import model.Hotel;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

public class HotelierClientRMI {
    private final HotelierClientRmiImp client;
    private HotelierServerInterface stubServer;
    private HotelierClientInterface stubClient;

    public HotelierClientRMI(String serverAddress,String rmiRemoteReference, int rmiPort) throws Exception{
        client = new HotelierClientRmiImp();
        Registry registry = LocateRegistry.getRegistry(serverAddress,rmiPort);
        stubServer = (HotelierServerInterface) registry.lookup(rmiRemoteReference);
        stubClient = (HotelierClientInterface) UnicastRemoteObject.exportObject(client,0);
    }

    public String requesteRegisterForNewUser(String username, String password) throws RemoteException{
        return stubServer.registerUser(username,password);
    }

    // richiede la registrazione di una callback per le città di interesse
    public void registerInterests(List<String> cities) throws RemoteException {

        // eseguo l' invocazione remota del metodo sul server per la registrazione di un callback per le città di interesse
        stubServer.registerCallback(stubClient, cities);
    }

    public void removeInterest() throws RemoteException{
        stubServer.unregisterCallback(stubClient);
    }

    // restituisce la mappa dei local rank formattata per la stampa
    public String localRankMapToString() {
        Map<String, List<Hotel>> localRankMap = client.getLocalRankMap();

        StringBuilder sb = new StringBuilder();
        String separator = "--------------------------------------------------\n";

        for (Map.Entry<String, List<Hotel>> entry : localRankMap.entrySet()) {
            sb.append("================" + entry.getKey() + "================").append("\n\n");

            for (Hotel hotel : entry.getValue()) {
                sb.append(hotel).append("\n");
                sb.append(separator);
            }
        }

        return sb.toString();
    }

    // restituisce true se utente non ha inserito città di interesse a seguito della login, false altrimenti
    public boolean islocalRankMapEmpty() {
        return client.getLocalRankMap().isEmpty();
    }

    // resetta mappa rank locali
    public void resetLocalRankMap() {
        client.getLocalRankMap().clear();
    }

    // chiudo le risorse associate alla comunicaizone tcp
    public void close() {
        try {
            // controllo se utente si era registrato per delle callback a seguito della logn
            if (!islocalRankMapEmpty()) {
                // deregistro utente dalle callback per le città di interesse
                removeInterest();
            }
            // rimuovo esportazione stub client per le callback
            UnicastRemoteObject.unexportObject(client, true);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
