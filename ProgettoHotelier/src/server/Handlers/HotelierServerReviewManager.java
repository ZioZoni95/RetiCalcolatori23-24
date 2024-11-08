package server.Handlers;

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

/**
 * La classe HotelierServerReviewManager gestisce le recensioni all'interno del sistema Hotelier.
 * Fornisce metodi per aggiungere recensioni, recuperare recensioni per un utente o per un hotel,
 * e serializzare/deserializzare le recensioni per la persistenza su disco tramite JSON.
 * Utilizza una lista sincronizzata per garantire l'accesso concorrente sicuro alle recensioni.
 */
public class HotelierServerReviewManager {

    // Istanza unica della classe (Singleton)
    private static HotelierServerReviewManager instance = null;

    /**
     * Metodo per ottenere l'istanza della classe (Singleton).
     *
     * @return L'istanza unica di HotelierServerReviewManager.
     */
    public static HotelierServerReviewManager getInstance() {
        if (instance == null) {
            instance = new HotelierServerReviewManager();
        }
        return instance;
    }

    // Lista di recensioni del sistema
    private List<Recensioni> reviews;

    /**
     * Costruttore della classe, inizializza la lista delle recensioni.
     */
    private HotelierServerReviewManager() {
        reviews = new ArrayList<>();
    }

    /**
     * Aggiunge una recensione alla lista delle recensioni.
     * La lista è sincronizzata per garantire che le operazioni di scrittura siano sicure in un contesto multithreading.
     *
     * @param review La recensione da aggiungere alla lista.
     */
    public void addReview(Recensioni review) {
        synchronized (reviews) {
            reviews.add(review);
        }
    }

    /**
     * Restituisce tutte le recensioni effettuate da un utente specifico.
     * Utilizza un ciclo per scorrere tutte le recensioni e confrontare l'username dell'utente.
     * La lista risultante è sincronizzata per evitare problemi di accesso concorrente.
     *
     * @param user L'utente di cui recuperare le recensioni.
     * @return Una lista di recensioni effettuate dall'utente.
     */
    public List<Recensioni> getUserReviews(Utente user) {
        List<Recensioni> userReviews = new LinkedList<>();
        synchronized (reviews) {
            for (Recensioni review : reviews) {
                // Confronta l'username in modo case insensitive
                if (StringUtils.equalsIgnoreCase(user.getUsername(), review.getUsername())) {
                    userReviews.add(review);
                }
            }
        }
        return userReviews;
    }

    /**
     * Restituisce tutte le recensioni associate a un hotel specifico.
     * La lista delle recensioni per un hotel viene filtrata per id dell'hotel e restituita.
     *
     * @param hotel L'hotel di cui recuperare le recensioni.
     * @return Una lista di recensioni per l'hotel specificato.
     */
    public List<Recensioni> getHotelReviews(Hotel hotel) {
        List<Recensioni> hotelReviews = new LinkedList<>();
        synchronized (reviews) {
            for (Recensioni review : reviews) {
                // Confronta l'ID dell'hotel nella recensione con l'ID dell'hotel passato
                if (review.gethotelID() == hotel.getId()) {
                    hotelReviews.add(review);
                }
            }
        }
        return hotelReviews;
    }

    /**
     * Serializza la lista delle recensioni in formato JSON e la scrive su un file per la persistenza.
     * Se si verifica un errore di scrittura, viene stampato lo stack trace.
     */
    public void serialize() {
        try {
            synchronized (reviews) {
                String reviewsJson = JsonUtils.serialize(reviews);
                // Scrive le recensioni serializzate su disco
                JsonUtils.writeFile(reviewsJson, new File(REVIEWS_PATH_JSON));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Deserializza le recensioni da un file JSON e le aggiunge alla lista delle recensioni nel sistema.
     * In caso di errore nella lettura o deserializzazione, viene stampato lo stack trace.
     */
    public void deserialize() {
        try {
            synchronized (reviews) {
                var reviewFile = new File(REVIEWS_PATH_JSON);
                var reviewsJSON = JsonUtils.readFile(reviewFile);
                var deserializedReviews = Arrays.asList(JsonUtils.deserialize(reviewsJSON, Recensioni[].class));
                // Aggiunge le recensioni deserializzate alla lista
                reviews.addAll(deserializedReviews);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
