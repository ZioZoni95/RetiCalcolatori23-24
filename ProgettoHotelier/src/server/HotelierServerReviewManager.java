package server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import model.Hotel;
import model.Recensioni;
import model.Utente;
import org.apache.commons.lang3.StringUtils;
import utils.JsonUtils;

import static server.config.ServerJsonSettings.REVIEWS_PATH_JSON;


public class HotelierServerReviewManager {

    /**
     * La classe HotelierServerReviewMAnager gestisce le recensioni all' interno di Hotelier.
     * Fornisce metodi per aggiungere recensioni, recuperare recensioni di un utente specifico e recensioni di un hotel specifico.
     * Utilizza una lista sincronizzata per garantire l'accesso concorrente alle recensioni e offre funzionalità per la serializzazione
     * e deserializzazione delle recensioni tramite JSON per la persistenza su disco.
     */

    private static HotelierServerReviewManager instance = null;

    public static HotelierServerReviewManager getInstance() {
        if (instance == null) {
            instance = new HotelierServerReviewManager();
        }
        return instance;
    }

    // lista di recensioni del registro
    private List<Recensioni> reviews;

    private HotelierServerReviewManager() {
        reviews = new ArrayList<>();
    }

    // aggiunge la recensione alla lista di recensioni del registro
    public void addReview(Recensioni review) {
        // acquisisco la lock sulla lista delle recensioni
        synchronized (reviews) {
            // aggiungo la recensione alla lista
            reviews.add(review);
        }
    }

    // restituisce la lista di recensioni effettuate da un utente
    public List<Recensioni> getUserReviews(Utente user) {

        // creo una nuova lista di recensioni
        List<Recensioni> userReviews = new LinkedList<>();
        // acquisisco la lock sulla lista delle recensioni
        synchronized (reviews) {
            // itero la lista di recensioni del registro
            for (Recensioni review : reviews) {
                // controllo che username dell' utente passato corrisponda a quello presente nella recensione (ignoreCase)
                if (StringUtils.equalsIgnoreCase(user.getUsername(), review.getUsername())) {
                    // aggiungo la recensione alla lista
                    userReviews.add(review);
                }
            }
        }
        // restituisco la lista di recensioni
        return userReviews;
    }

    // restituisce la lista di recensioni relative ad hotel passato
    public List<Recensioni> getHotelReviews(Hotel hotel) {

        // creo una nuova lista di recensioni
        List<Recensioni> hotelReviews = new LinkedList<>();
        // acquisisco la lock sulla lista delle recensioni
        synchronized (reviews) {
            // itero la lista di recensioni del registro
            for (Recensioni review : reviews) {
                // controllo che id hotel passato corrisponda a quello presente nella recensione
                if (review.gethotelID() == hotel.getId()) {
                    // aggiungo la recensione alla lista
                    hotelReviews.add(review);
                }
            }
        }
        // restituisco la lista di recensioni
        return hotelReviews;
    }

    // persiste la lista delle recensioni del registro sul disco
    public void serialize() {

        try {
            // acquisisco la lock sulla lista delle recensioni
            synchronized (reviews) {
                // serializzo la lista delle recensione in Json
                String reviewsJson = JsonUtils.serialize(reviews);
                // scrivo la lista seriliazzata sul file al path REVIEWS_PATH_JSON
                JsonUtils.writeFile(reviewsJson, new File(REVIEWS_PATH_JSON));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // deserializza la lista delle recensioni da disco e li aggiunge alla lista delle recensioni del registro
    public void deserialize() {

        try {
            // acquisisco la lock sulla lista delle recensioni
            synchronized (reviews) {
                // ottengo il file contenente la lista delle recensioni
                var reviewFile = new File(REVIEWS_PATH_JSON);
                // leggo la lista delle recensioni serializzata in Json
                var reviewsJSON = JsonUtils.readFile(reviewFile);
                // deserializzo la lista delle recensioni
                var deserializedReviews = Arrays.asList(JsonUtils.deserialize(reviewsJSON, Recensioni[].class));
                // aggiungo la lista delle recensioni deserializzata alla lista delle recensioni del registro
                reviews.addAll(deserializedReviews);
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
