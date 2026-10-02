package hotelier.model;

import java.io.Serializable;
import java.util.List;

/**
 * Un hotel con i suoi dati anagrafici e le statistiche aggregate delle recensioni.
 * Immutabile: ogni aggiornamento produce una nuova istanza.
 *
 * @param rate       punteggio medio complessivo (0-5)
 * @param ratings    medie dei singoli criteri
 * @param rank       punteggio di ranking globale calcolato dal server
 * @param localRank  posizione (1 = primo) nella classifica della città
 */
public record Hotel(
        int id,
        String name,
        String description,
        String city,
        String phone,
        List<String> services,
        double rate,
        Ratings ratings,
        int reviewCount,
        double rank,
        int localRank) implements Serializable {

    public Hotel {
        services = services == null ? List.of() : List.copyOf(services);
        ratings = ratings == null ? Ratings.ZERO : ratings;
    }

    /** L'hotel dopo l'aggiunta di una recensione con punteggio complessivo {@code overall}. */
    public Hotel withReview(int overall, Ratings reviewRatings) {
        return new Hotel(id, name, description, city, phone, services,
                (rate * reviewCount + overall) / (reviewCount + 1),
                ratings.runningAverage(reviewRatings, reviewCount),
                reviewCount + 1, rank, localRank);
    }

    public Hotel withRanking(double newRank, int newLocalRank) {
        return new Hotel(id, name, description, city, phone, services, rate, ratings, reviewCount,
                newRank, newLocalRank);
    }
}
