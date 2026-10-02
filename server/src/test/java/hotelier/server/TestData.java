package hotelier.server;

import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.util.JsonStore;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Dati di prova: due hotel a Roma e uno a Milano, senza recensioni. */
final class TestData {

    private TestData() {
    }

    static Hotel hotel(int id, String name, String city) {
        return new Hotel(id, name, "descrizione", city, "000", List.of("TV"), 0, Ratings.ZERO, 0, 0, 0);
    }

    static Services services(Path dir) throws IOException {
        JsonStore.write(dir.resolve("Hotels.json"), List.of(
                hotel(1, "Hotel Roma 1", "Roma"),
                hotel(2, "Hotel Roma 2", "Roma"),
                hotel(3, "Hotel Milano 1", "Milano")));
        var hotels = new HotelRepository(dir.resolve("Hotels.json"));
        var users = new UserService(dir.resolve("Users.json"));
        var reviews = new ReviewService(dir.resolve("Reviews.json"), users, hotels);
        return new Services(users, hotels, reviews, new SessionRegistry());
    }
}
