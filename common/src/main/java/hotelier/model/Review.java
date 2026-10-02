package hotelier.model;

import java.time.LocalDateTime;

/** Recensione di un utente su un hotel. */
public record Review(
        String username,
        int hotelID,
        int rate,
        Ratings rating,
        LocalDateTime timestamp) {

    public static Review now(String username, int hotelId, int rate, Ratings rating) {
        return new Review(username, hotelId, rate, rating, LocalDateTime.now());
    }
}
