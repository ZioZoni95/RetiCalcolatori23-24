package hotelier.server;

import hotelier.model.Badge;
import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.protocol.Message;
import hotelier.protocol.Message.*;
import hotelier.protocol.Wire;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Prova end-to-end: client reali su socket contro un {@link NioServer} reale. */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class ServerIntegrationTest {

    @TempDir
    Path dir;

    private Services services;
    private NioServer server;
    private int port;

    @BeforeEach
    void startServer() throws IOException {
        services = TestData.services(dir);
        server = new NioServer("127.0.0.1", 0, services);
        port = server.port();
        server.start();
        assertTrue(services.users().register("mario", "Pw1").ok());
        assertTrue(services.users().register("luigi", "Pw2").ok());
    }

    @AfterEach
    void stopServer() {
        server.close();
    }

    private static final class Client implements AutoCloseable {
        private final Socket socket;

        Client(int port) throws IOException {
            socket = new Socket("127.0.0.1", port);
        }

        Message request(Message message) throws IOException {
            Wire.write(socket.getOutputStream(), message);
            return Wire.read(socket.getInputStream());
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }

    @Test
    void loginSearchReviewAndBadge() throws IOException {
        try (Client client = new Client(port)) {
            assertInstanceOf(Failure.class, client.request(new BadgeRequest()), "serve il login");
            assertInstanceOf(Failure.class, client.request(new LoginRequest("mario", "sbagliata")));
            assertInstanceOf(Failure.class, client.request(new LoginRequest("mario", "pw1")), "password case sensitive");
            assertInstanceOf(Failure.class, client.request(new LoginRequest("nessuno", "x")));
            assertInstanceOf(Success.class, client.request(new LoginRequest("MARIO", "Pw1")));
            assertInstanceOf(Failure.class, client.request(new LoginRequest("mario", "Pw1")), "già loggato");

            Message hotel = client.request(new SearchHotelRequest("hotel roma 1", "ROMA"));
            assertEquals(1, assertInstanceOf(HotelResult.class, hotel).hotel().id());
            assertInstanceOf(Failure.class, client.request(new SearchHotelRequest("Hotel Inesistente", "Roma")));

            Message list = client.request(new SearchCityRequest("Roma"));
            assertEquals(2, assertInstanceOf(HotelListResult.class, list).hotels().size());
            assertInstanceOf(Failure.class, client.request(new SearchCityRequest("Atlantide")));

            assertInstanceOf(Success.class, client.request(
                    new InsertReviewRequest("Hotel Roma 1", "Roma", 4, new Ratings(3, 4, 5, 4))));
            assertInstanceOf(Success.class, client.request(
                    new InsertReviewRequest("Hotel Roma 1", "Roma", 2, new Ratings(1, 2, 3, 4))));

            Hotel updated = services.hotels().findByNameAndCity("Hotel Roma 1", "Roma").orElseThrow();
            assertEquals(2, updated.reviewCount());
            assertEquals(3.0, updated.rate(), 1e-9);
            assertEquals(new Ratings(2, 3, 4, 4), updated.ratings());

            assertEquals(Badge.EXPERT_REVIEWER, assertInstanceOf(BadgeResult.class,
                    client.request(new BadgeRequest())).badge());

            assertInstanceOf(Success.class, client.request(new LogoutRequest()));
            assertInstanceOf(Failure.class, client.request(new LogoutRequest()));
        }
    }

    @Test
    void reviewsAreValidatedByTheServer() throws IOException {
        try (Client client = new Client(port)) {
            assertInstanceOf(Failure.class, client.request(
                    new InsertReviewRequest("Hotel Roma 1", "Roma", 4, new Ratings(1, 1, 1, 1))), "serve il login");
            client.request(new LoginRequest("mario", "Pw1"));
            assertInstanceOf(Failure.class, client.request(
                    new InsertReviewRequest("Hotel Roma 1", "Roma", 6, new Ratings(1, 1, 1, 1))));
            assertInstanceOf(Failure.class, client.request(
                    new InsertReviewRequest("Hotel Roma 1", "Roma", 3, new Ratings(1, -1, 1, 1))));
            assertInstanceOf(Failure.class, client.request(
                    new InsertReviewRequest("Hotel Roma 1", "Roma", 3, null)));
            assertInstanceOf(Failure.class, client.request(
                    new InsertReviewRequest("Hotel Inesistente", "Roma", 3, new Ratings(1, 1, 1, 1))));
            assertEquals(0, services.hotels().findByNameAndCity("Hotel Roma 1", "Roma").orElseThrow().reviewCount());
        }
    }

    @Test
    void oneSessionPerUserAndDisconnectFreesIt() throws Exception {
        Client first = new Client(port);
        assertInstanceOf(Success.class, first.request(new LoginRequest("mario", "Pw1")));
        try (Client second = new Client(port)) {
            assertInstanceOf(Failure.class, second.request(new LoginRequest("mario", "Pw1")));
            assertInstanceOf(Success.class, second.request(new LoginRequest("luigi", "Pw2")));

            first.close(); // disconnessione senza logout
            Message retry = null;
            for (int i = 0; i < 100 && !(retry instanceof Success); i++) {
                try (Client third = new Client(port)) {
                    retry = third.request(new LoginRequest("mario", "Pw1"));
                }
                Thread.sleep(50);
            }
            assertInstanceOf(Success.class, retry, "dopo la disconnessione l'utente può rifare il login");
        }
    }

    @Test
    void pipelinedRequestsAreAnsweredInOrder() throws IOException {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            OutputStream out = socket.getOutputStream();
            List<Message> requests = List.of(
                    new LogoutRequest(),                         // Failure: non loggato
                    new LoginRequest("mario", "Pw1"),            // Success
                    new LogoutRequest(),                         // Success
                    new SearchCityRequest("Atlantide"));         // Failure
            for (Message request : requests) {
                out.write(Wire.encode(request)); // senza attendere le risposte
            }
            out.flush();

            InputStream in = socket.getInputStream();
            assertInstanceOf(Failure.class, Wire.read(in));
            assertInstanceOf(Success.class, Wire.read(in));
            assertInstanceOf(Success.class, Wire.read(in));
            assertInstanceOf(Failure.class, Wire.read(in));
        }
    }

    @Test
    void survivesGarbageAndMessagesSplitAcrossPackets() throws Exception {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            byte[] garbage = "non json".getBytes(StandardCharsets.UTF_8);
            out.write(ByteBuffer.allocate(4 + garbage.length).putInt(garbage.length).put(garbage).array());
            out.flush();
            assertInstanceOf(Failure.class, Wire.read(in), "messaggio non valido: errore ma connessione viva");

            byte[] frame = Wire.encode(new LoginRequest("mario", "Pw1"));
            for (byte b : frame) { // un byte alla volta
                out.write(b);
                out.flush();
            }
            assertInstanceOf(Success.class, Wire.read(in));
        }
    }

    @Test
    void oversizedFrameClosesTheConnection() throws IOException {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            socket.getOutputStream().write(ByteBuffer.allocate(4).putInt(Wire.MAX_FRAME_SIZE + 1).array());
            socket.getOutputStream().flush();
            assertEquals(-1, socket.getInputStream().read());
        }
    }

    @Test
    void serverHandlesManyConcurrentClients() throws Exception {
        int clients = 20;
        List<Thread> threads = new java.util.ArrayList<>();
        java.util.concurrent.atomic.AtomicInteger failures = new java.util.concurrent.atomic.AtomicInteger();
        for (int i = 0; i < clients; i++) {
            Thread t = new Thread(() -> {
                try (Client client = new Client(port)) {
                    for (int j = 0; j < 20; j++) {
                        if (!(client.request(new SearchCityRequest("Roma")) instanceof HotelListResult)) {
                            failures.incrementAndGet();
                        }
                    }
                } catch (IOException e) {
                    failures.incrementAndGet();
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        assertEquals(0, failures.get());
    }
}
