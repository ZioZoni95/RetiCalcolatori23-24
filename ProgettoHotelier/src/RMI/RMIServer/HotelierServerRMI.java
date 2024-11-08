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

/**
 * La classe `HotelierServerRMI` gestisce la connessione RMI (Remote Method Invocation) tra il client e il server Hotelier.
 * È responsabile per l'inizializzazione e l'esportazione dello stub che consente la comunicazione remota tra il client e il server.
 * Inoltre, gestisce la notifica ai client registrati riguardo ai cambiamenti nel ranking locale degli hotel.
 */
public class HotelierServerRMI {

    /**
     * La classe `HotelierRMIServerImp` fornisce l'implementazione dei metodi del server.
     */
    private final HotelierRMIServerImp serverImp;

    /**
     * L'oggetto `ObjectMapper` viene utilizzato per serializzare e deserializzare i dati in formato JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Costruttore che inizializza il server RMI e lo esporta come oggetto remoto.
     * Registra il server nel registry RMI per permettere al client di connettersi.
     *
     * @param rmiRemoteReference Il nome della referenza remota per il server.
     * @param rmiPort Il numero di porta per la connessione RMI.
     * @throws Exception Se ci sono errori nell'inizializzazione o esportazione dello stub RMI.
     */
    public HotelierServerRMI(String rmiRemoteReference, int rmiPort) throws Exception {
        serverImp = new HotelierRMIServerImp();
        objectMapper = new ObjectMapper(); // Utilizziamo ObjectMapper di Jackson per serializzare e deserializzare oggetti
        HotelierServerInterface stub = (HotelierServerInterface) UnicastRemoteObject.exportObject(serverImp, 0);
        LocateRegistry.createRegistry(rmiPort);  // Crea un registry RMI sulla porta specificata
        Registry registry = LocateRegistry.getRegistry(); // Ottieni il registry RMI
        registry.bind(rmiRemoteReference, stub); // Collega lo stub al registry sotto il nome specificato
    }

    /**
     * Metodo che notifica i client registrati riguardo a un cambiamento nel ranking locale degli hotel.
     * Questo metodo serializza il ranking in formato JSON e invia la notifica a tutti i client che hanno registrato interesse
     * per la città in questione. Se il client non è più disponibile (lancia un'eccezione), viene rimosso dalla lista dei client.
     *
     * @param localRank L'oggetto contenente il ranking locale da notificare ai client.
     * @throws JsonProcessingException Se si verifica un errore nella serializzazione del ranking.
     */
    public void notifyLocalRank(LocalHotelRanking localRank) throws JsonProcessingException {

        // Ottengo la mappa di callback dei client (chiave: client, valore: lista città di interesse)
        var clientsCallback = serverImp.getClientsCallback();

        // Inizializzo una lista di client da rimuovere in caso di errore durante la notifica
        List<HotelierClientInterface> clientsToRemove = new ArrayList<>();

        // Acquisisco la lock sulla mappa dei callback per evitare accessi concorrenti
        synchronized (clientsCallback) {

            // Itero su tutti i client e le loro città di interesse
            for (Map.Entry<HotelierClientInterface, List<String>> clientCallback : clientsCallback.entrySet()) {

                // Ottengo la lista di città di interesse del client
                var cities = clientCallback.getValue();
                // Ottengo la città associata al ranking locale
                var city = localRank.getCity();

                // Verifico se la città del ranking è tra quelle di interesse del client
                if (containsCity(cities, city)) {

                    // Se il client è interessato alla città, procedo con la notifica
                    try {
                        // Serializzo l'oggetto localRank in formato JSON
                        var serializedLocalRank = objectMapper.writeValueAsString(localRank);
                        // Ottengo lo stub del client
                        var clientInterface = clientCallback.getKey();

                        try {
                            // Invio la notifica del ranking locale al client
                            clientInterface.notifyInterest(serializedLocalRank);
                        } catch (RemoteException e) {
                            // Se si verifica un'eccezione, aggiungo il client alla lista di rimozione
                            clientsToRemove.add(clientInterface);
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Errore nella serializzazione del local rank", e);
                    }
                }
            }

            // Rimuovo i client che hanno lanciato un'eccezione durante la notifica
            for (HotelierClientInterface client : clientsToRemove) {
                clientsCallback.remove(client);
            }
        }
    }

    /**
     * Metodo di supporto per verificare se una determinata città è presente nella lista delle città di interesse di un client.
     * La ricerca è case-insensitive.
     *
     * @param cities La lista delle città di interesse.
     * @param cityToCheck La città da verificare.
     * @return true se la città è presente nella lista, false altrimenti.
     */
    private boolean containsCity(List<String> cities, String cityToCheck) {

        // Itero sulla lista delle città di interesse per verificare se la città passata è presente
        for (String city : cities) {

            // Confronto la città senza considerare la differenza tra maiuscole e minuscole
            if (StringUtils.equalsIgnoreCase(city, cityToCheck)) {
                return true;
            }
        }

        // Se la città non è trovata, restituisco false
        return false;
    }
}
