package hotelier.server.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import hotelier.server.EventLog;
import hotelier.server.ServerRuntime;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Dashboard testuale a schermo intero per il server: stato, statistiche, utenti collegati e log. */
public final class ServerTui {

    private final Path dataDir;
    private final MultiWindowTextGUI gui;
    private final BasicWindow window = new BasicWindow("Hotelier Server");
    private final Label status = new Label("");
    private final Label stats = new Label("");
    private final Table<String> usersTable = new Table<>("Utenti collegati");
    private final TextBox log = new TextBox(new TerminalSize(60, 10), TextBox.Style.MULTI_LINE).setReadOnly(true);
    private final Button toggleButton = new Button("Avvia server");
    private final Consumer<EventLog.Entry> eventListener;
    private ServerRuntime server;

    /** Apre la TUI sul terminale corrente e ritorna alla chiusura. */
    public static void run(Path dataDir) throws Exception {
        try (Screen screen = new DefaultTerminalFactory().createScreen()) {
            screen.startScreen();
            ServerTui tui = new ServerTui(screen, dataDir);
            tui.start();
            ScheduledExecutorService refresher = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "tui-refresh");
                thread.setDaemon(true);
                return thread;
            });
            refresher.scheduleWithFixedDelay(
                    () -> tui.gui.getGUIThread().invokeLater(tui::refresh), 1, 1, TimeUnit.SECONDS);
            tui.gui.addWindowAndWait(tui.window);
            refresher.shutdownNow();
            tui.stop();
        }
    }

    /** Per i test: la finestra viene aggiunta senza bloccare. */
    ServerTui(Screen screen, Path dataDir) {
        this.dataDir = dataDir;
        this.gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(),
                new EmptySpace(com.googlecode.lanterna.TextColor.ANSI.BLUE));
        this.eventListener = entry -> gui.getGUIThread().invokeLater(() -> appendLog(entry.toString()));
        buildWindow();
    }

    MultiWindowTextGUI gui() {
        return gui;
    }

    BasicWindow window() {
        return window;
    }

    private void buildWindow() {
        window.setHints(List.of(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));

        Panel header = new Panel(new LinearLayout());
        header.addComponent(status);
        header.addComponent(stats);

        usersTable.setPreferredSize(new TerminalSize(26, 8));
        Panel center = new Panel(new BorderLayout());
        center.addComponent(usersTable.withBorder(Borders.singleLine("Utenti")), BorderLayout.Location.LEFT);
        center.addComponent(log.withBorder(Borders.singleLine("Eventi")), BorderLayout.Location.CENTER);

        toggleButton.addListener(button -> toggle());
        Panel buttons = new Panel(new LinearLayout(com.googlecode.lanterna.gui2.Direction.HORIZONTAL));
        buttons.addComponent(toggleButton);
        buttons.addComponent(new Button("Ricalcola ranking", () -> {
            if (server != null) {
                server.rankNow();
                appendLog("[" + LocalDateTime.now().toLocalTime().withNano(0) + "] Ricalcolo del ranking richiesto");
            }
        }));
        buttons.addComponent(new Button("Esci", window::close));

        Panel root = new Panel(new BorderLayout());
        root.addComponent(header, BorderLayout.Location.TOP);
        root.addComponent(center, BorderLayout.Location.CENTER);
        root.addComponent(buttons, BorderLayout.Location.BOTTOM);
        window.setComponent(root);
        refresh();
    }

    /** Avvia il server; un errore (es. porta occupata) viene mostrato nel log. */
    void start() {
        if (server != null) {
            return;
        }
        try {
            server = ServerRuntime.start(dataDir);
            server.events().recent().forEach(entry -> appendLog(entry.toString()));
            server.events().addListener(eventListener);
        } catch (Exception e) {
            appendLog("[ERRORE] Impossibile avviare il server: " + e);
        }
        refresh();
    }

    void stop() {
        if (server != null) {
            server.events().removeListener(eventListener);
            server.close();
            appendLog("Server fermato");
            server = null;
        }
        refresh();
    }

    private void toggle() {
        if (server == null) {
            start();
            if (server == null) {
                MessageDialog.showMessageDialog(gui, "Errore", "Impossibile avviare il server: vedi il log degli eventi.");
            }
        } else {
            stop();
        }
    }

    void refresh() {
        toggleButton.setLabel(server == null ? "Avvia server" : "Ferma server");
        usersTable.getTableModel().clear();
        if (server == null) {
            status.setText(" ■ SERVER FERMO");
            stats.setText(" ");
            return;
        }
        var config = server.config();
        status.setText(" ● SERVER IN ESECUZIONE   TCP " + config.tcpPort() + "   RMI " + config.rmiPort()
                + "   Multicast " + config.mcastAddress() + ":" + config.mcastPort());
        List<String> loggedIn = server.loggedInUsers();
        stats.setText(" Client connessi: " + server.connectedClients()
                + "   Utenti collegati: " + loggedIn.size()
                + "   Utenti registrati: " + server.users().size()
                + "   Hotel: " + server.hotels().size()
                + "   Recensioni: " + server.reviewCount()
                + "   Uptime: " + format(server.uptime()));
        loggedIn.forEach(user -> usersTable.getTableModel().addRow(user));
    }

    private void appendLog(String line) {
        log.addLine(line);
        log.setCaretPosition(log.getLineCount(), 0);
    }

    private static String format(Duration duration) {
        return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
    }
}
