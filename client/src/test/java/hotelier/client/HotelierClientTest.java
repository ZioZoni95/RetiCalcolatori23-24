package hotelier.client;

import hotelier.model.Badge;
import hotelier.model.CityRanking;
import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.protocol.Message.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 60, unit = TimeUnit.SECONDS)
class HotelierClientTest {

    @TempDir
    Path dir;

    private TestServer server;
    private HotelierClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = TestServer.start(dir);
        client = server.connect();
    }

    @AfterEach
    void tearDown() {
        client.close();
        server.close();
    }

    @Test
    void registerLoginAndSearch() throws Exception {
        assertTrue(client.register("mario", "Pw1").ok());
        assertFalse(client.register("mario", "altra").ok());

        assertFalse(client.isLoggedIn());
        assertInstanceOf(Failure.class, client.login("mario", "sbagliata"));
        assertInstanceOf(Success.class, client.login("mario", "Pw1"));
        assertEquals("mario", client.username().orElseThrow());

        assertEquals(2, assertInstanceOf(HotelListResult.class, client.searchCity("roma")).hotels().size());
        assertEquals("Hotel Milano 1", assertInstanceOf(HotelResult.class,
                client.searchHotel("hotel milano 1", "MILANO")).hotel().name());
        assertInstanceOf(Failure.class, client.searchCity("Atlantide"));
    }

    @Test
    void reviewsUpdateTheBadge() throws Exception {
        client.register("mario", "Pw1");
        client.login("mario", "Pw1");
        assertEquals(Badge.REVIEWER, assertInstanceOf(BadgeResult.class, client.badge()).badge());
        for (int i = 0; i < 2; i++) {
            assertInstanceOf(Success.class, client.insertReview("Hotel Roma 1", "Roma", 4, new Ratings(4, 4, 4, 4)));
        }
        assertEquals(Badge.EXPERT_REVIEWER, assertInstanceOf(BadgeResult.class, client.badge()).badge());
        assertEquals(2, server.runtime.reviewCount());
    }

    @Test
    void logoutClearsTheSessionAndTheInterests() throws Exception {
        client.register("mario", "Pw1");
        client.login("mario", "Pw1");
        client.setInterests(List.of("Roma"));
        assertEquals(List.of("Roma"), client.interests());

        assertInstanceOf(Success.class, client.logout());
        assertFalse(client.isLoggedIn());
        assertTrue(client.interests().isEmpty());
        assertTrue(client.rankings().isEmpty());
        assertInstanceOf(Failure.class, client.badge());
    }

    @Test
    void interestsReceiveTheCurrentRankingAndLaterChanges() throws Exception {
        client.register("mario", "Pw1");
        client.login("mario", "Pw1");

        client.setInterests(List.of("Roma", "Milano"));
        List<CityRanking> initial = client.rankings(); // il server invia subito la classifica corrente
        assertEquals(List.of("Milano", "Roma"), initial.stream().map(CityRanking::city).toList());

        CountDownLatch changed = new CountDownLatch(1);
        client.addRankingListener(changed::countDown);

        // una recensione ottima all'hotel 2 lo porta in testa alla classifica di Roma
        client.insertReview("Hotel Roma 2", "Roma", 5, new Ratings(5, 5, 5, 5));
        server.runtime.rankNow();
        assertTrue(changed.await(10, TimeUnit.SECONDS), "callback RMI non ricevuta");

        CityRanking rome = client.rankings().stream().filter(r -> r.city().equals("Roma")).findFirst().orElseThrow();
        assertEquals(List.of("Hotel Roma 2", "Hotel Roma 1"), rome.hotels().stream().map(Hotel::name).toList());

        // l'interesse sostituito per Milano fa sparire Roma
        client.setInterests(List.of("Milano"));
        assertEquals(List.of("Milano"), client.rankings().stream().map(CityRanking::city).toList());
    }

    @Test
    void connectionFailureIsReportedClearly() {
        server.close();
        assertThrows(Exception.class, () -> HotelierClient.connect(server.clientConfig));
    }
}
