package server;

import model.Hotel;
import org.apache.commons.lang3.StringUtils;
import utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static server.config.ServerJsonSettings.HOTELS_PATH_JSON;

public class HotelierServerHotelManager {

    /**
     * La HotelierServerHotelManager gestisce gli hotel all' interno di Hotelier.
     * Fornisce metodi per recuperare, aggiornare e gestire informazioni sugli hotel, inclusa la ricerca per ID, nome e città.
     * Utilizza una lista sincronizzata per garantire l'accesso concorrente agli hotel e offre funzionalità per la serializzazione
     * e deserializzazione degli hotel tramite JSON per la persistenza su disco.
     */

    private static HotelierServerHotelManager instance = null;

    public static HotelierServerHotelManager getInstance() {
        if (instance == null) {
            instance = new HotelierServerHotelManager();
        }
        return instance;
    }

    // lista degli hotel del registro
    private List<Hotel> hotels;

    private HotelierServerHotelManager() {
        // inzializzo la lista di hotel a ArryList
        hotels = new ArrayList<>();
    }

    // restituisce hotel avente id passato, null se hotel non trovato
    public Hotel getHotelByID(int hotelID) {
        // acquisisco la lock sulla lista di hotel
        synchronized (hotels) {
            // itero la lista di tutti gli hotel del registro
            for (var hotel : hotels) {
                // controllo se id hotel corrisponde a quello passato
                if (hotel.getId() == hotelID) {
                    // restituisco hotel
                    return hotel;
                }
            }
        }
        // restituisco null
        return null;
    }

    // restituisce list di hotel aventi città passata
    public List<Hotel> getHotelsByCity(String city) {

        // creo una nuova lista di hotel
        List<Hotel> hotelsCity = new ArrayList<>();
        List<Hotel> copyHotels;
        // acquisisco la lock sulla lista di hotel
        synchronized (hotels) {
            copyHotels = new ArrayList<>(hotels);
        }
        //    System.out.println("SONO NELLA SYNCRO!!");
            // itero la lista di tutti gli hotel del registro
            for (Hotel hotel : copyHotels) {
                // controllo se città hotel corrisponde a quella passato (ingnoreCase)
                if (StringUtils.equalsIgnoreCase(hotel.getCity(), city)) {
                    // aggiungo hotel a hotelsCity
                    hotelsCity.add(hotel);
                }
            }

        // restituisco la lista di hotel
        return hotelsCity;
    }

    // restituisce hotel avente id e città passati, null se hotel non trovato
    public Hotel getHotelByNameAndCity(String hotelName, String city) {

        // ottengo la lista di hotel aventi città passata
        List<Hotel> cityHotels = getHotelsByCity(city);
        // acquisisco la lock sulla lista di hotel
        synchronized (hotels) {
            // itero la lista di hotel
            for (var hotel : cityHotels) {
                // controllo se nome hotel corrisponde a quella passato (ingnoreCase)
                if (StringUtils.equalsIgnoreCase(hotel.getName(), hotelName)) {
                    // restituisco hotel
                    return hotel;
                }
            }
        }
        // restituisco null
        return null;
    }

    // restituisce la lista di tutte le città degli hotel presenti nel registro
    public List<String> getCities() {

        // creo un set di città per evitare duplicati
        Set<String> cities = new HashSet<>();
        // acquisisco la lock sulla lista di hotel
        synchronized (hotels) {
            // itero la lista di tutti gli hotel del registro
            for (Hotel hotel : hotels) {
                // aggiungo hotel a hotelsCity
                cities.add(hotel.getCity());
            }
        }

        // restituisco lista delle città
        return new ArrayList<>(cities);
    }

    // restituisce lista degli hotel del registro
    public List<Hotel> getHotels() {
        // restituisco lista degli hotel del registro
        return hotels;
    }

    // persiste la lista di hotel del registro sul disco
    public void serialize() {

        try {
            // acquisisco la lock sulla lista di hotel
            synchronized (hotels) {
                // serializzo la lista di hotel in Json
                String hotelsJson = JsonUtils.serialize(hotels);
                // scrivo la lista seriliazzata sul file al path HOTELS_PATH_JSON
                JsonUtils.writeFile(hotelsJson, new File(HOTELS_PATH_JSON));
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // deserializza la lista di hotel da disco e li aggiunge alla lista di hotel del registro
    public void deserialize() {

        try {
            // acquisisco la lock sulla lista di hotel
            synchronized (hotels) {
                // ottengo il file contenente la lista di hotel
                var hotelFile = new File(HOTELS_PATH_JSON);
                // leggo la lista di hotel serializzata in Json
                var hotelsJSON = JsonUtils.readFile(hotelFile);
                // deserializzo la lista di hotel
                var deserializedHotels = Arrays.asList(JsonUtils.deserialize(hotelsJSON, Hotel[].class));
                // aggiungo la lista di hotel deserializzata alla lista di hotel del registro
                hotels.addAll(deserializedHotels);
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}