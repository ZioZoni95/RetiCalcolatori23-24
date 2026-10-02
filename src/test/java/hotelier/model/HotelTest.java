package hotelier.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HotelTest {

    private static Hotel hotel() {
        return new Hotel(1, "Hotel", "d", "Roma", "1", List.of("TV"), 0, Ratings.ZERO, 0, 0, 0);
    }

    @Test
    void reviewsUpdateRunningAverages() {
        Hotel h = hotel()
                .withReview(4, new Ratings(2, 4, 3, 5))
                .withReview(2, new Ratings(4, 4, 1, 1));
        assertEquals(2, h.reviewCount());
        assertEquals(3.0, h.rate(), 1e-9);
        assertEquals(new Ratings(3, 4, 2, 3), h.ratings());
    }
}
