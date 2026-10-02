package hotelier.server;

import hotelier.config.ServerConfig;
import hotelier.protocol.Message.*;
import hotelier.protocol.Wire;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class ServerRuntimeTest {

    @TempDir
    Path dir;

    private static void waitUntil(java.util.function.BooleanSupplier condition) throws InterruptedException {
        for (int i = 0; i < 100 && !condition.getAsBoolean(); i++) {
            Thread.sleep(50);
        }
        assertTrue(condition.getAsBoolean());
    }

    @Test
    void startsServesAndExposesStatistics() throws Exception {
        ServerConfig config = TestData.prepareDataDir(dir);
        try (ServerRuntime server = ServerRuntime.start(dir)) {
            assertEquals(3, server.hotels().size());
            assertEquals(0, server.users().size());
            assertEquals(0, server.connectedClients());
            assertTrue(server.events().recent().stream().anyMatch(e -> e.text().contains("Server in esecuzione")));

            try (Socket socket = new Socket("127.0.0.1", config.tcpPort())) {
                waitUntil(() -> server.connectedClients() == 1);
                Wire.write(socket.getOutputStream(), new LoginRequest("nessuno", "x"));
                assertInstanceOf(Failure.class, Wire.read(socket.getInputStream()));
            }
            waitUntil(() -> server.connectedClients() == 0);
            assertTrue(server.events().recent().stream().anyMatch(e -> e.text().startsWith("Client disconnesso")));
        }
    }

    @Test
    void loggedInUsersAreVisibleAndEventsAreRecorded() throws Exception {
        ServerConfig config = TestData.prepareDataDir(dir);
        try (ServerRuntime server = ServerRuntime.start(dir)) {
            // la registrazione passa da RMI
            var rmi = (hotelier.rmi.ServerService) java.rmi.registry.LocateRegistry
                    .getRegistry("127.0.0.1", config.rmiPort()).lookup(config.rmiRemoteReference());
            assertTrue(rmi.register("Mario", "Pw1").ok());

            try (Socket socket = new Socket("127.0.0.1", config.tcpPort())) {
                Wire.write(socket.getOutputStream(), new LoginRequest("mario", "Pw1"));
                assertInstanceOf(Success.class, Wire.read(socket.getInputStream()));
                assertEquals(java.util.List.of("Mario"), server.loggedInUsers());
                Wire.write(socket.getOutputStream(), new InsertReviewRequest("Hotel Roma 1", "Roma", 5,
                        new hotelier.model.Ratings(5, 5, 5, 5)));
                assertInstanceOf(Success.class, Wire.read(socket.getInputStream()));
            }
            assertEquals(1, server.reviewCount());
            waitUntil(() -> server.loggedInUsers().isEmpty());
            var texts = server.events().recent().stream().map(EventLog.Entry::text).toList();
            assertTrue(texts.contains("Registrazione (RMI): Mario"), texts.toString());
            assertTrue(texts.contains("Login: Mario"), texts.toString());
            assertTrue(texts.stream().anyMatch(t -> t.startsWith("Recensione di Mario su Hotel Roma 1")));
            assertTrue(texts.contains("Logout: Mario"), texts.toString());
        }
    }

    @Test
    void canBeStoppedAndRestartedOnTheSamePorts() throws Exception {
        TestData.prepareDataDir(dir);
        ServerRuntime first = ServerRuntime.start(dir);
        first.close();
        first.close(); // idempotente
        try (ServerRuntime second = ServerRuntime.start(dir)) {
            assertEquals(3, second.hotels().size());
        }
    }

    @Test
    void startFailsClearlyWithoutHotelsFile() {
        var e = assertThrows(java.io.IOException.class, () -> ServerRuntime.start(dir, ServerConfig.defaults()));
        assertTrue(e.getMessage().contains("Hotels.json"));
    }

    @Test
    void rejectsAPortAlreadyInUseAndReleasesTheOthers() throws Exception {
        ServerConfig config = TestData.prepareDataDir(dir);
        try (var busy = new java.net.ServerSocket(config.tcpPort(), 1, java.net.InetAddress.getByName("127.0.0.1"))) {
            assertThrows(Exception.class, () -> ServerRuntime.start(dir));
        }
        try (ServerRuntime server = ServerRuntime.start(dir)) { // RMI e multicast sono stati rilasciati
            assertNotNull(server);
        }
    }
}
