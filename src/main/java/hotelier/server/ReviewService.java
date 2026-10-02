package hotelier.server;

import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.model.Review;
import hotelier.util.JsonStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Recensioni pubblicate, persistite su file JSON. */
public final class ReviewService {

    private static final System.Logger LOG = System.getLogger(ReviewService.class.getName());

    private final Path file;
    private final UserService users;
    private final HotelRepository hotels;
    private final List<Review> reviews = new ArrayList<>();
    private final Map<Integer, List<Review>> byHotel = new HashMap<>();

    public ReviewService(Path file, UserService users, HotelRepository hotels) throws IOException {
        this.file = file;
        this.users = users;
        this.hotels = hotels;
        if (Files.exists(file)) {
            for (Review review : JsonStore.read(file, Review[].class)) {
                index(review);
            }
        } else {
            JsonStore.write(file, reviews);
        }
    }

    /**
     * Pubblica una recensione: la salva e aggiorna in modo coerente le statistiche
     * dell'hotel e il badge dell'utente.
     */
    public synchronized void submit(String username, Hotel hotel, int rate, Ratings ratings) {
        index(Review.now(username, hotel.id(), rate, ratings));
        try {
            JsonStore.write(file, reviews);
        } catch (IOException e) {
            LOG.log(System.Logger.Level.ERROR, "Impossibile salvare " + file, e);
        }
        users.recordReview(username);
        hotels.addReview(hotel.id(), rate, ratings);
    }

    public synchronized List<Review> forHotel(int hotelId) {
        return List.copyOf(byHotel.getOrDefault(hotelId, List.of()));
    }

    private void index(Review review) {
        reviews.add(review);
        byHotel.computeIfAbsent(review.hotelID(), id -> new ArrayList<>()).add(review);
    }
}
