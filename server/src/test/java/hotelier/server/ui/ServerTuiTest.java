package hotelier.server.ui;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;
import hotelier.config.ServerConfig;
import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.util.JsonStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerTuiTest {

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

    static int freePort() throws IOException {
        try (var socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    void showsStatusStatisticsAndEvents() throws Exception {
        JsonStore.write(dir.resolve("Hotels.json"), List.of(
                new Hotel(1, "Hotel Roma 1", "d", "Roma", "1", List.of(), 0, Ratings.ZERO, 0, 0, 0)));
        JsonStore.write(dir.resolve("ServerConfig.json"), new ServerConfig(freePort(), freePort(), freePort(), 3600,
                "127.0.0.1", "Hotelier-Test", "230.0.0.1"));

        Screen screen = new TerminalScreen(new DefaultVirtualTerminal(new TerminalSize(140, 30)));
        screen.startScreen();
        ServerTui tui = new ServerTui(screen, dir);
        tui.gui().addWindow(tui.window());

        tui.gui().updateScreen();
        assertTrue(dump(screen).contains("SERVER FERMO"), dump(screen));
        assertTrue(dump(screen).contains("Avvia server"));

        tui.start();
        tui.refresh();
        tui.gui().updateScreen();
        String running = dump(screen);
        assertTrue(running.contains("SERVER IN ESECUZIONE"), running);
        assertTrue(running.contains("Client connessi: 0"), running);
        assertTrue(running.contains("Hotel: 1"), running);
        assertTrue(running.contains("Server in esecuzione: TCP"), running);
        assertTrue(running.contains("Ferma server"));

        tui.stop();
        tui.gui().updateScreen();
        assertTrue(dump(screen).contains("SERVER FERMO"));
        screen.stopScreen();
    }
}
