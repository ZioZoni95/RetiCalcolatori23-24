package hotelier.client.ui;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;
import hotelier.client.HotelierClient;
import hotelier.client.TestServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 60, unit = TimeUnit.SECONDS)
class ClientTuiTest {

    @TempDir
    Path dir;

    private static String dump(Screen screen) {
        TerminalSize size = screen.getTerminalSize();
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < size.getRows(); row++) {
            for (int col = 0; col < size.getColumns(); col++) {
                text.append(screen.getFrontCharacter(new TerminalPosition(col, row)).getCharacterString());
            }
            text.append('\n');
        }
        return text.toString();
    }

    @Test
    void rendersMenuAndRunsOperations() throws Exception {
        try (TestServer server = TestServer.start(dir); HotelierClient client = server.connect()) {
            Screen screen = new TerminalScreen(new DefaultVirtualTerminal(new TerminalSize(130, 40)));
            screen.startScreen();
            ClientTui tui = new ClientTui(client, screen, Runnable::run);
            tui.gui().addWindow(tui.window());
            tui.gui().updateScreen();

            String initial = dump(screen);
            for (String item : List.of("Registrati", "Login", "Logout", "Cerca hotel", "Hotel di una città",
                    "Nuova recensione", "Il mio badge", "Città di interesse", "Classifiche locali", "Esci",
                    "Risultati", "Notifiche", "Non hai effettuato il login")) {
                assertTrue(initial.contains(item), item + "\n" + initial);
            }

            assertTrue(tui.register("mario", "Pw1").contains("Nuovo Utente : mario"));
            assertTrue(tui.login("mario", "sbagliata").contains("Password errata!"));
            assertTrue(tui.login("mario", "Pw1").contains("Login effettuato correttamente!"));
            tui.gui().getGUIThread().processEventsAndUpdate();
            assertTrue(tui.headerText().contains("Utente: mario"), tui.headerText());

            assertEquals("Città di interesse: Roma, Milano", tui.interests("Roma, Milano"));
            assertEquals(2, client.rankings().size());

            assertTrue(tui.review(List.of("Hotel Roma 1", "Roma", "5", "4", "4", "4", "4"))
                    .contains("Recensione registrata con successo."));
            assertTrue(tui.review(List.of("Hotel Roma 1", "Roma", "9", "4", "4", "4", "4")).contains("tra 0 e 5"));
            assertTrue(tui.review(List.of("Hotel Roma 1", "Roma", "x", "4", "4", "4", "4")).contains("tra 0 e 5"));
            assertTrue(tui.badge().contains("Badge: Recensore"));

            assertTrue(tui.logout().contains("Logout effettuato correttamente"));
            tui.gui().getGUIThread().processEventsAndUpdate();
            assertTrue(tui.headerText().contains("Non hai effettuato il login"), tui.headerText());
            screen.stopScreen();
        }
    }
}
