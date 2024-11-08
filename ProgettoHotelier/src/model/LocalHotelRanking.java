package model;

import java.util.ArrayList;
import java.util.List;

/**
 * La classe LocalHotelRanking rappresenta una lista di hotel classificati in una città specifica.
 * Ogni istanza di questa classe è associata a una città e contiene una lista di hotel situati in quella città.
 * La classe fornisce metodi per aggiungere hotel alla lista, ottenere la città e la lista di hotel,
 * nonché impostare una nuova lista di hotel.
 */
public class LocalHotelRanking {

    private String city;  // La città a cui si riferisce la classifica degli hotel
    private List<Hotel> hotels;  // La lista di hotel nella città

    // Costruttore senza parametri
    public LocalHotelRanking(){}

    /**
     * Costruttore che inizializza la città e crea una lista vuota di hotel.
     *
     * @param city Il nome della città per la quale viene creata la classifica
     */
    public LocalHotelRanking(String city) {
        this.city = city;
        this.hotels = new ArrayList<>();  // Inizializza la lista vuota di hotel
    }

    /**
     * Aggiunge un hotel alla lista di hotel nella città specificata.
     *
     * @param hotel L'hotel da aggiungere alla classifica
     */
    public void add(Hotel hotel) {
        hotels.add(hotel);  // Aggiunge l'hotel alla lista
    }

    /**
     * Restituisce il nome della città a cui si riferisce la classifica.
     *
     * @return Il nome della città
     */
    public String getCity() {
        return city;
    }

    /**
     * Restituisce la lista di hotel associati alla città.
     *
     * @return La lista di hotel
     */
    public List<Hotel> getHotels() {
        return hotels;
    }

    /**
     * Imposta una nuova lista di hotel per la classifica.
     *
     * @param hotels La nuova lista di hotel
     */
    public void setHotels(List<Hotel> hotels) {
        this.hotels = hotels;  // Imposta la nuova lista di hotel
    }

}
