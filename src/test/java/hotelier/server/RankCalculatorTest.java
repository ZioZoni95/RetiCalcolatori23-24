package hotelier.server;

import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.model.Review;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankCalculatorTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2025, 1, 1, 12, 0);

    private static Hotel hotel(double rate, double sub, int count) {
        return new Hotel(1, "H", "d", "Roma", "1", List.of(), rate, new Ratings(sub, sub, sub, sub), count, 0, 0);
    }

    private static List<Review> reviews(int count, LocalDateTime when) {
        return Collections.nCopies(count, new Review("u", 1, 5, Ratings.ZERO, when));
    }

    @Test
    void noReviewsMeansZero() {
        assertEquals(0, RankCalculator.rank(hotel(0, 0, 0), List.of(), NOW));
    }

    @Test
    void staysWithinZeroAndFive() {
        double rank = RankCalculator.rank(hotel(5, 5, 1000), reviews(1000, NOW), NOW);
        assertTrue(rank > 4.9 && rank <= 5, "rank = " + rank);
    }

    @Test
    void betterQualityRanksHigher() {
        double good = RankCalculator.rank(hotel(4.5, 4.5, 5), reviews(5, NOW), NOW);
        double bad = RankCalculator.rank(hotel(2, 2, 5), reviews(5, NOW), NOW);
        assertTrue(good > bad);
    }

    @Test
    void moreReviewsRankHigher() {
        double many = RankCalculator.rank(hotel(4, 4, 30), reviews(30, NOW), NOW);
        double few = RankCalculator.rank(hotel(4, 4, 1), reviews(1, NOW), NOW);
        assertTrue(many > few);
    }

    @Test
    void recentReviewsRankHigher() {
        double recent = RankCalculator.rank(hotel(4, 4, 5), reviews(5, NOW.minusDays(1)), NOW);
        double old = RankCalculator.rank(hotel(4, 4, 5), reviews(5, NOW.minusDays(365)), NOW);
        assertTrue(recent > old);
    }
}
