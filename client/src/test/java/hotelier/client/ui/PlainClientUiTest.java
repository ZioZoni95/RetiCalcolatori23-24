package hotelier.client.ui;

import hotelier.client.HotelierClient;
import hotelier.client.TestServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 60, unit = TimeUnit.SECONDS)
class PlainClientUiTest {

    @TempDir
    Path dir;

    @Test
    void scriptedSession() throws Exception {
        try (TestServer server = TestServer.start(dir); HotelierClient client = server.connect()) {
            String script = String.join("\n",
                    "help extra",
                    "register \"mario\" \"Pw1\"",
                    "login \"mario\" \"pw1\"",
                    "login \"mario\" \"Pw1\"",
                    "\"Roma\"",
                    "searchHotel \"hotel roma 1\" \"roma\"",
                    "insertReview \"Hotel Roma 1\" \"Roma\" \"4\" \"4\" \"4\" \"4\" \"4\"",
                    "showMyBadges",
                    "showLocalRanks",
                    "logout",
                    "showLocalRanks",
                    "exit",
                    "login \"mai\" \"eseguito\"") + "\n";
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            new PlainClientUi(client, new BufferedReader(new StringReader(script)),
                    new PrintStream(bytes, true, StandardCharsets.UTF_8)).run();
            String out = bytes.toString(StandardCharsets.UTF_8);

            assertTrue(out.contains("racchiudere ogni argomento tra virgolette"), out);
            assertTrue(out.contains("Nuovo Utente : mario"), out);
            assertTrue(out.contains("Password errata!"), out);
            assertTrue(out.contains("Login effettuato correttamente!"), out);
            assertTrue(out.contains("Città di interesse registrate con successo!"), out);
            assertTrue(out.contains("Nome: Hotel Roma 1"), out);
            assertTrue(out.contains("Recensione registrata con successo."), out);
            assertTrue(out.contains("Badge: Recensore"), out);
            assertTrue(out.contains("================ Roma ================"), out);
            assertTrue(out.contains("Logout effettuato correttamente"), out);
            assertTrue(out.contains("Nessuna classifica locale disponibile"), out);
            assertFalse(out.contains("eseguito"), "dopo exit non si esegue altro");
        }
    }

    @Test
    void endOfInputStopsTheLoop() throws Exception {
        try (TestServer server = TestServer.start(dir); HotelierClient client = server.connect()) {
            new PlainClientUi(client, new BufferedReader(new StringReader("")),
                    new PrintStream(new ByteArrayOutputStream())).run();
        }
    }
}
