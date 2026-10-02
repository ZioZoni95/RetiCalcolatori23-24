package hotelier.client.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import hotelier.client.HotelierClient;
import hotelier.client.ui.TuiForm.Field;
import hotelier.model.Ratings;
import hotelier.protocol.Message;
import hotelier.protocol.Message.Success;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/** Interfaccia testuale a schermo intero per il client: menu, risultati e notifiche. */
public final class ClientTui {

    private final HotelierClient client;
    private final Executor background;
    private final MultiWindowTextGUI gui;
    private final BasicWindow window = new BasicWindow("Hotelier");
    private final Label header = new Label("");
    private final TextBox output = new TextBox(new TerminalSize(70, 18), TextBox.Style.MULTI_LINE).setReadOnly(true);
    private final TextBox notifications = new TextBox(new TerminalSize(70, 4), TextBox.Style.MULTI_LINE).setReadOnly(true);
    private boolean showingRankings;

    /** Apre la TUI sul terminale corrente e ritorna alla chiusura. */
    public static void run(HotelierClient client) throws IOException {
        try (Screen screen = new DefaultTerminalFactory().createScreen()) {
            screen.startScreen();
            ClientTui tui = new ClientTui(client, screen, Executors.newCachedThreadPool(r -> {
                Thread thread = new Thread(r, "tui-request");
                thread.setDaemon(true);
                return thread;
            }));
            tui.gui.addWindowAndWait(tui.window);
        }
    }

    /** Per i test: l'esecutore delle richieste di rete può essere sincrono. */
    ClientTui(HotelierClient client, Screen screen, Executor background) {
        this.client = client;
        this.background = background;
        this.gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        buildWindow();
        client.addNotificationListener(text -> ui(() -> notify(text)));
        client.addRankingListener(() -> ui(() -> {
            notify("Classifica locale aggiornata");
            if (showingRankings) {
                showRankings();
            }
        }));
    }

    MultiWindowTextGUI gui() {
        return gui;
    }

    BasicWindow window() {
        return window;
    }

    String outputText() {
        return output.getText();
    }

    String notificationsText() {
        return notifications.getText();
    }

    String headerText() {
        return header.getText();
    }

    // ---------------------------------------------------------------- costruzione

    private void buildWindow() {
        window.setHints(List.of(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));

        ActionListBox menu = new ActionListBox(new TerminalSize(24, 12));
        menu.addItem("Registrati", () -> credentialsForm("Registrazione", this::register));
        menu.addItem("Login", () -> credentialsForm("Login", this::login));
        menu.addItem("Logout", () -> async(this::logout));
        menu.addItem("Cerca hotel", this::searchHotelForm);
        menu.addItem("Hotel di una città", this::searchCityForm);
        menu.addItem("Nuova recensione", this::reviewForm);
        menu.addItem("Il mio badge", () -> async(this::badge));
        menu.addItem("Città di interesse", this::interestsForm);
        menu.addItem("Classifiche locali", this::showRankings);
        menu.addItem("Guida", () -> show(Formatter.HELP));
        menu.addItem("Esci", window::close);

        Panel root = new Panel(new BorderLayout());
        root.addComponent(header, BorderLayout.Location.TOP);
        root.addComponent(menu.withBorder(Borders.singleLine("Menu")), BorderLayout.Location.LEFT);
        root.addComponent(output.withBorder(Borders.singleLine("Risultati")), BorderLayout.Location.CENTER);
        root.addComponent(notifications.withBorder(Borders.singleLine("Notifiche")), BorderLayout.Location.BOTTOM);
        window.setComponent(root);

        updateHeader();
        show("Benvenuto su Hotelier!\n\nScegli un'operazione dal menu (frecce + Invio, Tab per spostarti tra i riquadri).\n"
                + "Premi \"Guida\" per l'elenco dei comandi.");
    }

    // ---------------------------------------------------------------- azioni con form

    private void credentialsForm(String title, CredentialsAction action) {
        TuiForm.show(gui, title, List.of(Field.text("Username"), Field.secret("Password")))
                .ifPresent(values -> async(() -> action.run(values.get(0), values.get(1))));
    }

    private void searchHotelForm() {
        TuiForm.show(gui, "Cerca hotel", List.of(Field.text("Nome hotel"), Field.text("Città")))
                .ifPresent(v -> async(() -> Formatter.response(client.searchHotel(v.get(0), v.get(1)))));
    }

    private void searchCityForm() {
        TuiForm.show(gui, "Hotel di una città", List.of(Field.text("Città")))
                .ifPresent(v -> async(() -> Formatter.response(client.searchCity(v.get(0)))));
    }

    private void reviewForm() {
        if (!client.isLoggedIn()) {
            show("Effettua il login per inserire una recensione.");
            return;
        }
        TuiForm.show(gui, "Nuova recensione (punteggi da 0 a 5)", List.of(
                        Field.text("Nome hotel"), Field.text("Città"), Field.text("Punteggio globale"),
                        Field.text("Pulizia"), Field.text("Posizione"), Field.text("Servizi"), Field.text("Qualità")))
                .ifPresent(v -> async(() -> review(v)));
    }

    private void interestsForm() {
        if (!client.isLoggedIn()) {
            show("Effettua il login per scegliere le città di interesse.");
            return;
        }
        TuiForm.show(gui, "Città di interesse",
                        List.of(new Field("Città (separate da virgola)", String.join(", ", client.interests()), false)))
                .ifPresent(v -> async(() -> interests(v.get(0))));
    }

    // ---------------------------------------------------------------- operazioni (thread di rete)

    private interface CredentialsAction {
        String run(String username, String password) throws IOException;
    }

    String register(String username, String password) throws RemoteException {
        return client.register(username, password).message();
    }

    String login(String username, String password) throws IOException {
        Message response = client.login(username, password);
        String text = Formatter.response(response);
        if (response instanceof Success) {
            text += "\n\nUsa \"Città di interesse\" per ricevere le variazioni delle classifiche locali.";
        }
        ui(this::updateHeader);
        return text;
    }

    String logout() throws IOException {
        String text = Formatter.response(client.logout());
        ui(this::updateHeader);
        return text;
    }

    String badge() throws IOException {
        return Formatter.response(client.badge());
    }

    String review(List<String> values) throws IOException {
        int[] scores = new int[5];
        for (int i = 0; i < 5; i++) {
            try {
                scores[i] = Integer.parseInt(values.get(i + 2));
            } catch (NumberFormatException e) {
                scores[i] = -1;
            }
            if (scores[i] < 0 || scores[i] > 5) {
                return "I punteggi devono essere numeri interi tra 0 e 5.";
            }
        }
        return Formatter.response(client.insertReview(values.get(0), values.get(1), scores[0],
                new Ratings(scores[1], scores[2], scores[3], scores[4])));
    }

    String interests(String commaSeparated) throws RemoteException {
        List<String> cities = Arrays.stream(commaSeparated.split(","))
                .map(String::strip).filter(city -> !city.isEmpty()).toList();
        client.setInterests(cities);
        ui(this::updateHeader);
        return cities.isEmpty() ? "Nessuna città di interesse." : "Città di interesse: " + String.join(", ", cities);
    }

    // ---------------------------------------------------------------- aiuti

    /** Esegue l'operazione fuori dal thread dell'interfaccia e ne mostra l'esito. */
    private void async(Callable<String> operation) {
        background.execute(() -> {
            String text;
            try {
                text = operation.call();
            } catch (IOException e) {
                text = "Errore di connessione: impossibile comunicare con il server di Hotelier (" + e.getMessage() + ")";
            } catch (Exception e) {
                text = "Errore: " + e;
            }
            String result = text;
            ui(() -> show(result));
        });
    }

    private void ui(Runnable task) {
        gui.getGUIThread().invokeLater(task);
    }

    private void show(String text) {
        showingRankings = false;
        output.setText(text);
        output.setCaretPosition(0, 0);
    }

    private void showRankings() {
        output.setText(Formatter.localRankings(client.rankings()));
        output.setCaretPosition(0, 0);
        showingRankings = true;
    }

    private void notify(String text) {
        notifications.addLine("[" + LocalTime.now().withNano(0) + "] " + text);
        notifications.setCaretPosition(notifications.getLineCount(), 0);
    }

    private void updateHeader() {
        header.setText(" HOTELIER  |  " + client.username()
                .map(user -> "Utente: " + user)
                .orElse("Non hai effettuato il login")
                + client.interests().stream().collect(Collectors.joining(", ", client.interests().isEmpty() ? "" : "  |  Città: ", "")));
    }
}
