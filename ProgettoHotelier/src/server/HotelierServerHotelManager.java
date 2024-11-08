package server;

import model.Hotel;
import org.apache.commons.lang3.StringUtils;
import utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static server.config.ServerJsonSettings.HOTELS_PATH_JSON;

/**
 * La classe HotelierServerHotelManager gestisce gli hotel all'interno di Hotelier.
 * Fornisce metodi per recuperare, aggiornare e gestire informazioni sugli hotel,
 * inclusa la ricerca per ID, nome e città. Utilizza una lista sincronizzata per garantire
 * l'accesso concorrente agli hotel e offre funzionalità per la serializzazione
 * e deserializzazione degli hotel tramite JSON per la persistenza su disco.
 */
public class HotelierServerHotelManager {

    // Variabile di istanza per il pattern Singleton
    private static HotelierServerHotelManager instance = null;

    /**
     * Restituisce l'istanza singola della classe.
     *
     * @return L'istanza singola di HotelierServerHotelManager.
     */
    public static HotelierServerHotelManager getInstance() {
        if (instance == null) {
            instance = new HotelierServerHotelManager();
        }
        return instance;
    }

    // Lista degli hotel nel registro
    private List<Hotel> hotels;

    /**
     * Costruttore della classe. Inizializza la lista di hotel.
     */
    private HotelierServerHotelManager() {
        // Inizializzo la lista di hotel come un ArrayList
        hotels = new ArrayList<>();
    }

    /**
     * Restituisce un hotel tramite il suo ID.
     *
     * @param hotelID L'ID dell'hotel.
     * @return L'hotel con l'ID specificato, oppure null se non trovato.
     */
    public Hotel getHotelByID(int hotelID) {
        // Acquisisco la lock sulla lista di hotel per garantire la sincronizzazione
        synchronized (hotels) {
            // Itero la lista di tutti gli hotel nel registro
            for (var hotel : hotels) {
                // Controllo se l'ID dell'hotel corrisponde a quello passato
                if (hotel.getId() == hotelID) {
                    return hotel; // Restituisco l'hotel trovato
                }
            }
        }
        return null; // Se non trovato, restituisco null
    }

    /**
     * Restituisce una lista di hotel situati nella città specificata.
     *
     * @param city La città in cui cercare gli hotel.
     * @return Una lista di hotel nella città specificata.
     */
    public List<Hotel> getHotelsByCity(String city) {
        // Creo una nuova lista per gli hotel nella città
        List<Hotel> hotelsCity = new ArrayList<>();
        List<Hotel> copyHotels;

        // Acquisisco la lock sulla lista di hotel per garantire la sincronizzazione
        synchronized (hotels) {
            copyHotels = new ArrayList<>(hotels); // Creo una copia della lista di hotel
        }

        // Itero la lista degli hotel
        for (Hotel hotel : copyHotels) {
            // Controllo se la città dell'hotel corrisponde a quella passata (ignorando maiuscole/minuscole)
            if (StringUtils.equalsIgnoreCase(hotel.getCity(), city)) {
                hotelsCity.add(hotel); // Aggiungo l'hotel alla lista
            }
        }

        // Restituisco la lista degli hotel nella città
        return hotelsCity;
    }

    /**
     * Restituisce un hotel in base al nome e alla città.
     *
     * @param hotelName Il nome dell'hotel.
     * @param city La città in cui si trova l'hotel.
     * @return L'hotel che corrisponde sia al nome che alla città, o null se non trovato.
     */
    public Hotel getHotelByNameAndCity(String hotelName, String city) {
        // Ottengo la lista di hotel situati nella città specificata
        List<Hotel> cityHotels = getHotelsByCity(city);

        // Acquisisco la lock sulla lista di hotel
        synchronized (hotels) {
            // Itero attraverso gli hotel nella città
            for (var hotel : cityHotels) {
                // Controllo se il nome dell'hotel corrisponde a quello passato (ignorando maiuscole/minuscole)
                if (StringUtils.equalsIgnoreCase(hotel.getName(), hotelName)) {
                    return hotel; // Restituisco l'hotel trovato
                }
            }
        }
        return null; // Se non trovato, restituisco null
    }

    /**
     * Restituisce la lista di tutte le città in cui sono presenti hotel.
     *
     * @return Una lista di tutte le città.
     */
    public List<String> getCities() {
        // Creo un set per evitare duplicati nelle città
        Set<String> cities = new HashSet<>();

        // Acquisisco la lock sulla lista di hotel
        synchronized (hotels) {
            // Itero la lista di hotel per estrarre le città uniche
            for (Hotel hotel : hotels) {
                cities.add(hotel.getCity()); // Aggiungo la città all'insieme
            }
        }

        // Restituisco la lista delle città
        return new ArrayList<>(cities);
    }

    /**
     * Restituisce la lista completa di tutti gli hotel nel registro.
     *
     * @return Una lista di tutti gli hotel.
     */
    public List<Hotel> getHotels() {
        // Restituisco la lista di tutti gli hotel
        return hotels;
    }

    /**
     * Serializza la lista di hotel nel registro e la salva su disco in formato JSON.
     */
    public void serialize() {
        try {
            // Acquisisco la lock sulla lista di hotel per garantire la sincronizzazione
            synchronized (hotels) {
                // Serializzo la lista di hotel in formato JSON
                String hotelsJson = JsonUtils.serialize(hotels);
                // Scrivo il JSON nel file di persistenza definito dal path HOTELS_PATH_JSON
                JsonUtils.writeFile(hotelsJson, new File(HOTELS_PATH_JSON));
            }
        } catch (IOException exception) {
            exception.printStackTrace(); // Gestisco eventuali errori di I/O
        }
    }

    /**
     * Deserializza la lista di hotel dal disco e la aggiunge alla lista degli hotel nel registro.
     */
    public void deserialize() {
        try {
            // Acquisisco la lock sulla lista di hotel
            synchronized (hotels) {
                // Ottengo il file contenente gli hotel serializzati in formato JSON
                var hotelFile = new File(HOTELS_PATH_JSON);
                // Leggo il contenuto del file JSON
                var hotelsJSON = JsonUtils.readFile(hotelFile);
                // Deserializzo i dati JSON nel formato di un array di Hotel
                var deserializedHotels = Arrays.asList(JsonUtils.deserialize(hotelsJSON, Hotel[].class));
                // Aggiungo gli hotel deserializzati alla lista di hotel
                hotels.addAll(deserializedHotels);
            }
        } catch (IOException exception) {
            exception.printStackTrace(); // Gestisco eventuali errori di I/O
        }
    }
}
