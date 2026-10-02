package hotelier.client.ui;

import hotelier.client.HotelierClient;
import hotelier.client.TestServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

/** Il pannello Swing si può costruire e pilotare anche senza display. */
@Timeout(value = 90, unit = TimeUnit.SECONDS)
class ClientGuiTest {

    @TempDir
    Path dir;

    private static <T extends Component> T byName(Container root, String name, Class<T> type) {
        for (Component child : root.getComponents()) {
            if (name.equals(child.getName()) && type.isInstance(child)) {
                return type.cast(child);
            }
            if (child instanceof Container container) {
                T found = byNameOrNull(container, name, type);
                if (found != null) {
                    return found;
                }
            }
        }
        throw new AssertionError("componente non trovato: " + name);
    }

    private static <T extends Component> T byNameOrNull(Container root, String name, Class<T> type) {
        try {
            return byName(root, name, type);
        } catch (AssertionError e) {
            return null;
        }
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        for (int i = 0; i < 200 && !condition.getAsBoolean(); i++) {
            Thread.sleep(50);
        }
        assertTrue(condition.getAsBoolean(), "condizione non raggiunta");
    }

    private static void onEdt(Runnable task) throws Exception {
        SwingUtilities.invokeAndWait(task);
    }

    @Test
    void fullSession() throws Exception {
        try (TestServer server = TestServer.start(dir); HotelierClient client = server.connect()) {
            ClientGui gui = new ClientGui(client);
            JTextField username = byName(gui, "username", JTextField.class);
            JTextField password = byName(gui, "password", JTextField.class);
            JLabel message = byName(gui, "message", JLabel.class);
            AbstractButton login = byName(gui, "loginButton", AbstractButton.class);
            AbstractButton logout = byName(gui, "logoutButton", AbstractButton.class);

            assertTrue(login.isEnabled());
            assertFalse(logout.isEnabled());
            assertFalse(byName(gui, "reviewButton", AbstractButton.class).isEnabled(), "serve il login");

            // registrazione
            onEdt(() -> {
                username.setText("mario");
                password.setText("Pw1");
                byName(gui, "registerButton", AbstractButton.class).doClick();
            });
            await(() -> message.getText().contains("Nuovo Utente : mario"));

            // password errata, poi login corretto
            onEdt(() -> {
                password.setText("sbagliata");
                login.doClick();
            });
            await(() -> message.getText().contains("Password errata!"));
            onEdt(() -> {
                password.setText("Pw1");
                login.doClick();
            });
            await(() -> client.isLoggedIn() && !login.isEnabled() && logout.isEnabled());
            assertTrue(byName(gui, "reviewButton", AbstractButton.class).isEnabled());

            // ricerca: tutti gli hotel di una città e dettaglio della selezione
            JTable hotels = byName(gui, "hotelsTable", JTable.class);
            onEdt(() -> {
                byName(gui, "searchCity", JTextField.class).setText("Roma");
                byName(gui, "searchButton", AbstractButton.class).doClick();
            });
            await(() -> hotels.getRowCount() == 2);
            onEdt(() -> hotels.setRowSelectionInterval(0, 0));
            JTextArea detail = byName(gui, "detail", JTextArea.class);
            await(() -> detail.getText().contains("Nome: Hotel Roma"));

            // un hotel preciso
            onEdt(() -> {
                byName(gui, "searchName", JTextField.class).setText("hotel milano 1");
                byName(gui, "searchCity", JTextField.class).setText("Milano");
                byName(gui, "searchButton", AbstractButton.class).doClick();
            });
            await(() -> hotels.getRowCount() == 1 && "Hotel Milano 1".equals(hotels.getValueAt(0, 0)));

            // recensione precompilata dalla selezione
            onEdt(() -> byName(gui, "reviewThisButton", AbstractButton.class).doClick());
            assertEquals("Hotel Milano 1", byName(gui, "reviewHotel", JTextField.class).getText());
            onEdt(() -> {
                byName(gui, "score0", JSpinner.class).setValue(5);
                byName(gui, "reviewButton", AbstractButton.class).doClick();
            });
            await(() -> message.getText().contains("Recensione registrata con successo."));
            assertEquals(1, server.runtime.reviewCount());

            // città di interesse e classifiche
            JTable rankings = byName(gui, "rankingsTable", JTable.class);
            onEdt(() -> {
                byName(gui, "interests", JTextField.class).setText("Roma, Milano");
                byName(gui, "interestsButton", AbstractButton.class).doClick();
            });
            await(() -> rankings.getRowCount() == 3);

            // logout
            onEdt(logout::doClick);
            await(() -> !client.isLoggedIn() && login.isEnabled() && rankings.getRowCount() == 0);
        }
    }
}
