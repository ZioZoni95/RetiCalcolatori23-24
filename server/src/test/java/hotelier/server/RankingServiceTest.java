package hotelier.server;

import hotelier.model.CityRanking;
import hotelier.model.Hotel;
import hotelier.model.Ratings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RankingServiceTest {

    @TempDir
    Path dir;

    private final List<Hotel> firstPlaces = new ArrayList<>();
    private final List<CityRanking> rankings = new ArrayList<>();

    @Test
    void notifiesWhenTheFirstHotelOfACityChanges() throws IOException {
        Services services = TestData.services(dir);
        var ranking = new RankingService(services.hotels(), services.reviews(), firstPlaces::add, rankings::add);

        // nessuna recensione: classifica invariata, nessuna notifica
        ranking.update(LocalDateTime.now());
        assertTrue(firstPlaces.isEmpty());
        assertTrue(rankings.isEmpty());

        // l'hotel 2 riceve una recensione ottima e supera l'hotel 1
        services.users().register("mario", "pw");
        services.reviews().submit("mario", services.hotels().findByNameAndCity("Hotel Roma 2", "Roma").orElseThrow(),
                5, new Ratings(5, 5, 5, 5));
        ranking.update(LocalDateTime.now());

        assertEquals(1, firstPlaces.size());
        assertEquals("Hotel Roma 2", firstPlaces.get(0).name());
        assertEquals(1, rankings.size());
        assertEquals("Roma", rankings.get(0).city());
        assertEquals(List.of(2, 1), rankings.get(0).hotels().stream().map(Hotel::id).toList());

        // stato persistito: ranking e posizione locale
        Hotel second = services.hotels().findByNameAndCity("Hotel Roma 2", "Roma").orElseThrow();
        assertTrue(second.rank() > 0);
        assertEquals(1, second.localRank());
        assertEquals(2, services.hotels().findByNameAndCity("Hotel Roma 1", "Roma").orElseThrow().localRank());

        // nessun cambiamento: nessuna nuova notifica
        ranking.update(LocalDateTime.now());
        assertEquals(1, firstPlaces.size());
        assertEquals(1, rankings.size());
    }

    @Test
    void changeBelowTheFirstPlaceOnlyUpdatesTheRanking() throws IOException {
        Services services = TestData.services(dir);
        // l'hotel 1 è primo; con due recensioni ottime l'hotel 3 (altra città) non c'entra
        services.users().register("mario", "pw");
        var h1 = services.hotels().findByNameAndCity("Hotel Roma 1", "Roma").orElseThrow();
        services.reviews().submit("mario", h1, 5, new Ratings(5, 5, 5, 5));
        var ranking = new RankingService(services.hotels(), services.reviews(), firstPlaces::add, rankings::add);
        ranking.update(LocalDateTime.now());
        firstPlaces.clear();
        rankings.clear();

        // un'altra recensione per l'hotel 3 non tocca la classifica di Roma
        var h3 = services.hotels().findByNameAndCity("Hotel Milano 1", "Milano").orElseThrow();
        services.reviews().submit("mario", h3, 3, new Ratings(3, 3, 3, 3));
        ranking.update(LocalDateTime.now());
        assertTrue(firstPlaces.isEmpty());
        assertTrue(rankings.isEmpty());
    }
}
