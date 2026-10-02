package hotelier.server.ui;

import hotelier.config.ServerConfig;
import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.util.JsonStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Il pannello Swing si può costruire anche senza display: solo la finestra (JFrame) lo richiede. */
class ServerGuiTest {

    @TempDir
    Path dir;

    private static <T extends Component> List<T> find(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (Component child : root.getComponents()) {
            if (type.isInstance(child)) {
                found.add(type.cast(child));
            }
            if (child instanceof Container container) {
                found.addAll(find(container, type));
            }
        }
        return found;
    }

    private static int freePort() throws IOException {
        try (var socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    void startsAndStopsTheServerAndShowsData() throws Exception {
        JsonStore.write(dir.resolve("Hotels.json"), List.of(
                new Hotel(1, "Hotel Roma 1", "d", "Roma", "1", List.of(), 0, Ratings.ZERO, 0, 0, 0),
                new Hotel(2, "Hotel Roma 2", "d", "Roma", "1", List.of(), 0, Ratings.ZERO, 0, 0, 0)));
        JsonStore.write(dir.resolve("ServerConfig.json"), new ServerConfig(freePort(), freePort(), freePort(), 3600,
                "127.0.0.1", "Hotelier-Test", "230.0.0.1"));

        ServerGui gui = new ServerGui(dir);
        try {
            assertFalse(gui.isRunning());
            assertTrue(find(gui, JLabel.class).stream().anyMatch(l -> l.getText().contains("Server fermo")));

            gui.start();
            assertTrue(gui.isRunning());
            assertTrue(find(gui, JLabel.class).stream().anyMatch(l -> l.getText().contains("Server in esecuzione")));

            // tabella degli hotel: selezionando la scheda viene popolata
            JTabbedPane tabs = find(gui, JTabbedPane.class).get(0);
            tabs.setSelectedIndex(2);
            gui.refresh();
            JTable hotels = find((Container) tabs.getComponentAt(2), JTable.class).get(0);
            assertEquals(2, hotels.getRowCount());
            assertEquals("Hotel Roma 1", hotels.getValueAt(0, 1));

            gui.stop();
            assertFalse(gui.isRunning());
            assertTrue(find(gui, JLabel.class).stream().anyMatch(l -> l.getText().contains("Server fermo")));
        } finally {
            gui.shutdown();
        }
    }

    @Test
    void reportsAStartFailureWithoutCrashing() {
        ServerGui gui = new ServerGui(dir); // nessun Hotels.json
        try {
            gui.start();
            assertFalse(gui.isRunning());
        } finally {
            gui.shutdown();
        }
    }
}
