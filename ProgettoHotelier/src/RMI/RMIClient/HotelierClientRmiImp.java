package RMI.RMIClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Hotel;
import model.LocalHotelRanking;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * La classe `HotelierClientRmiImp` implementa l'interfaccia `HotelierClientInterface` e gestisce le callback ricevute dal server RMI.
 * Essa è responsabile della gestione della mappa `localRankMap`, che memorizza i ranghi locali degli hotel per città.
 * La mappa utilizza il nome della città come chiave e la lista degli hotel ordinati per rank locale come valore.
 * Quando viene ricevuta una callback dal server, la classe deserializza i dati, aggiornando i ranghi locali per la città corrispondente.
 * La sincronizzazione viene utilizzata per proteggere l'accesso concorrente alla mappa e mantenere la coerenza dei dati.
 */
public class HotelierClientRmiImp implements HotelierClientInterface {

    /**
     * Mappa che contiene i ranghi locali per ciascuna città.
     * La chiave è il nome della città, mentre il valore è una lista di oggetti `Hotel` ordinati per rank locale.
     */
    private final Map<String, List<Hotel>> localRankMap;

    /**
     * Oggetto `ObjectMapper` di Jackson utilizzato per la deserializzazione dei dati JSON ricevuti dal server.
     * Viene utilizzato per convertire la stringa JSON ricevuta in un oggetto di tipo `LocalHotelRanking`.
     */
    private final ObjectMapper objectMapper;

    /**
     * Costruttore della classe. Inizializza la mappa dei ranghi locali e l'oggetto `ObjectMapper` per la gestione della deserializzazione.
     * La mappa dei ranghi locali viene inizializzata come una nuova `HashMap`, mentre `objectMapper` è configurato per utilizzare la libreria Jackson.
     */
    public HotelierClientRmiImp() {
        localRankMap = new HashMap<>();  // Inizializzazione della mappa che contiene i ranghi locali per ciascuna città.
        objectMapper = new ObjectMapper();  // Inizializzazione di ObjectMapper per la deserializzazione dei dati JSON.
    }

    /**
     * Metodo che viene invocato dal server remoto (`HotelierServerRmi`) per notificare un aggiornamento del rank locale di una città.
     * La stringa JSON che rappresenta il rank locale viene deserializzata in un oggetto `LocalHotelRanking` contenente la città
     * e la lista degli hotel ordinati. La mappa dei ranghi locali viene quindi aggiornata con i nuovi dati.
     * La sincronizzazione è utilizzata per garantire che l'accesso alla mappa avvenga in modo sicuro in ambienti multi-thread.
     *
     * @param serializedLocalRank La stringa JSON contenente il rank locale da deserializzare.
     * @throws RemoteException Se si verifica un errore durante la deserializzazione o l'aggiornamento della mappa.
     */
    @Override
    public synchronized void notifyInterest(String serializedLocalRank) throws RemoteException {

        try {
            // Deserializzazione del rank locale dalla stringa JSON
            LocalHotelRanking localRank = objectMapper.readValue(serializedLocalRank, LocalHotelRanking.class);

            // Estrazione del nome della città dal rank locale
            String city = localRank.getCity();

            // Estrazione della lista degli hotel ordinati per rank locale dalla struttura LocalHotelRanking
            List<Hotel> hotels = localRank.getHotels();

            // Aggiornamento della mappa dei ranghi locali con la città e la lista di hotel ordinati
            synchronized (localRankMap) {
                localRankMap.put(city, hotels);  // Aggiorna la mappa con la nuova lista di hotel per la città.
            }
        } catch (Exception e) {
            // Se si verifica un errore durante la deserializzazione, viene lanciata un'eccezione RemoteException
            throw new RemoteException("Errore durante la deserializzazione del Rank locale", e);
        }
    }

    /**
     * Restituisce la mappa contenente i ranghi locali degli hotel, con la città come chiave e la lista degli hotel come valore.
     * Questa mappa è aggiornata con le informazioni ricevute dalle callback dal server remoto.
     *
     * @return La mappa dei ranghi locali, dove la chiave è il nome della città e il valore è la lista degli hotel ordinati.
     */
    public Map<String, List<Hotel>> getLocalRankMap() {
        return localRankMap;  // Restituisce la mappa aggiornata dei ranghi locali.
    }
}
