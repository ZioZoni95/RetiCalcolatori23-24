package RMI.RMIServer;

import RMI.RMIClient.HotelierClientInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.LocalHotelRanking;
import org.apache.commons.lang3.StringUtils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HotelierServerRMI {
    /**
     * La classe HotelierServerRmi gestisce la connessione RMI (Remote Method
     * Invocation) tra il client e il server Hotelier. Inizializza e esporta lo
     * stub per la comunicazione remota, permettendo al client di invocare
     * metodi remoti sul server. Permette l' invocazione dei seguenti metodi
     * remoti su HotelierClientRmi: • callback notifica cambiamento prima
     * posizione rank locale;
     */

    private final HotelierRMIServerImp serverImp;
    private final ObjectMapper objectMapper;

    public HotelierServerRMI(String rmiRemoteReference, int rmiPort) throws Exception {
        serverImp = new HotelierRMIServerImp();
        objectMapper = new ObjectMapper();
        HotelierServerInterface stub = (HotelierServerInterface) UnicastRemoteObject.exportObject(serverImp, 0);
        LocateRegistry.createRegistry(rmiPort);
        Registry registry = LocateRegistry.getRegistry();
        registry.bind(rmiRemoteReference, stub);
    }

    public void notifyLocalRank(LocalHotelRanking localRank) throws JsonProcessingException {

        // ottengo la mappa delle callback
        var clientsCallback = serverImp.getClientsCallback();

        // inizializzo una lista di clientRmiInterface
        List<HotelierClientInterface> clientsToRemove = new ArrayList<>();

        // acquisisco la lock sulla mappa
        synchronized (clientsCallback) {
            // itero la mappa delle callback
            for (Map.Entry<HotelierClientInterface, List<String>> clientCallback : clientsCallback.entrySet()) {

                // ottengo la lista di città di interesse del client
                var cities = clientCallback.getValue();
                // ottengo la città di localRank
                var city = localRank.getCity();
                // controllo se il client aveva registrato interesse per la città di localRank
                if (containsCity(cities, city)) {

                    // interesse registrato
                    try {
                        // serializzo local rank in Json
                        var serializedLocalRank = objectMapper.writeValueAsString(localRank);
                        // ottengo stub del client
                        var clientInterface = clientCallback.getKey();

                        try {
                            clientInterface.notifyInterest(serializedLocalRank);
                        } catch (RemoteException e) {
                            clientsToRemove.add(clientInterface);
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // Rimuovo dalla mappa delle callback tutti i client noticati che hanno sollevato un'eccezione
            for (HotelierClientInterface client : clientsToRemove) {
                clientsCallback.remove(client);
            }
        }
    }

    // Restituisce true se cities contiene cityToCheck (ignoreCase), false altrimenti
    private boolean containsCity(List<String> cities, String cityToCheck) {

        for (String city : cities) {

            if (StringUtils.equalsIgnoreCase(city, cityToCheck)) {
                return true;
            }
        }

        return false;
    }
}


