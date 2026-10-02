package hotelier.server;

import hotelier.model.Hotel;
import hotelier.model.Review;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Punteggio di ranking di un hotel, nell'intervallo [0, 5]. Tiene conto di:
 * <ul>
 *   <li><b>qualità</b>: media tra punteggio complessivo e media dei criteri;</li>
 *   <li><b>quantità</b>: {@code n / (n + 10)}, cresce con il numero di recensioni e satura;</li>
 *   <li><b>attualità</b>: media dei pesi {@code 0.5^(giorni / 90)} delle recensioni.</li>
 * </ul>
 * {@code rank = qualità × (0.5 + 0.3 × quantità + 0.2 × attualità)}. Senza recensioni vale 0.
 */
public final class RankCalculator {

    static final double BASE_WEIGHT = 0.5;
    static final double QUANTITY_WEIGHT = 0.3;
    static final double RECENCY_WEIGHT = 0.2;
    static final double QUANTITY_HALF_POINT = 10;
    static final double RECENCY_HALF_LIFE_DAYS = 90;

    private RankCalculator() {
    }

    public static double rank(Hotel hotel, List<Review> reviews, LocalDateTime now) {
        if (hotel.reviewCount() == 0) {
            return 0;
        }
        double quality = (hotel.rate() + hotel.ratings().mean()) / 2;
        double quantity = hotel.reviewCount() / (hotel.reviewCount() + QUANTITY_HALF_POINT);
        double recency = reviews.stream()
                .mapToDouble(r -> recencyWeight(r.timestamp(), now))
                .average()
                .orElse(0);
        return quality * (BASE_WEIGHT + QUANTITY_WEIGHT * quantity + RECENCY_WEIGHT * recency);
    }

    static double recencyWeight(LocalDateTime timestamp, LocalDateTime now) {
        double days = Math.max(0, Duration.between(timestamp, now).toMinutes()) / (24.0 * 60);
        return Math.pow(0.5, days / RECENCY_HALF_LIFE_DAYS);
    }
}
