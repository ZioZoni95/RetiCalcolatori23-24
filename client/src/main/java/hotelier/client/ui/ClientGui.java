package hotelier.client.ui;

import hotelier.client.HotelierClient;
import hotelier.model.CityRanking;
import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.protocol.Message;
import hotelier.protocol.Message.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Interfaccia grafica del client: login/registrazione, ricerca hotel, recensioni e classifiche locali.
 * Le richieste di rete girano fuori dal thread Swing; non ci sono finestre modali.
 */
public final class ClientGui extends JPanel {

    private static final Color OK_COLOR = new Color(0x1B7F3B);
    private static final Color ERROR_COLOR = new Color(0xB3261E);

    private final HotelierClient client;

    private final JTextField username = named(new JTextField(12), "username");
    private final JPasswordField password = named(new JPasswordField(12), "password");
    private final JButton loginButton = named(new JButton("Login"), "loginButton");
    private final JButton registerButton = named(new JButton("Registrati"), "registerButton");
    private final JButton logoutButton = named(new JButton("Logout"), "logoutButton");
    private final JLabel userLabel = new JLabel();
    private final JLabel message = named(new JLabel(" "), "message");

    private final JTextField searchCity = named(new JTextField(14), "searchCity");
    private final JTextField searchName = named(new JTextField(18), "searchName");
    private final JButton searchButton = named(new JButton("Cerca"), "searchButton");
    private final JButton reviewThisButton = named(new JButton("Scrivi una recensione"), "reviewThisButton");
    private final DefaultTableModel hotelsModel = readOnlyModel("Nome", "Città", "Punteggio", "Recensioni", "Rank",
            "Rank locale");
    private final JTable hotelsTable = named(new JTable(hotelsModel), "hotelsTable");
    private final JTextArea detail = named(new JTextArea(8, 40), "detail");
    private List<Hotel> shownHotels = List.of();

    private final JTextField reviewHotel = named(new JTextField(18), "reviewHotel");
    private final JTextField reviewCity = named(new JTextField(14), "reviewCity");
    private final JSpinner[] scores = new JSpinner[5];
    private final JButton reviewButton = named(new JButton("Invia recensione"), "reviewButton");

    private final JTextField interests = named(new JTextField(30), "interests");
    private final JButton interestsButton = named(new JButton("Imposta città di interesse"), "interestsButton");
    private final DefaultTableModel rankingsModel = readOnlyModel("Città", "Pos.", "Hotel", "Punteggio", "Rank");
    private final JTable rankingsTable = named(new JTable(rankingsModel), "rankingsTable");

    private final JTextArea notifications = named(new JTextArea(5, 40), "notifications");
    private final JTabbedPane tabs = new JTabbedPane();

    public static void run(HotelierClient client) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // si usa il look and feel di default
            }
            JFrame frame = new JFrame("Hotelier");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    frame.dispose();
                    client.close();
                    System.exit(0);
                }
            });
            frame.setContentPane(new ClientGui(client));
            frame.setSize(960, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /** Segnala un errore all'avvio: con la GUI in una finestra, altrimenti non fa nulla (c'è già stderr). */
    public static void showFatalError(String text) {
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(null, text, "Hotelier", JOptionPane.ERROR_MESSAGE);
        }
    }

    public ClientGui(HotelierClient client) {
        super(new BorderLayout(8, 8));
        this.client = client;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildTop(), BorderLayout.NORTH);
        tabs.addTab("Cerca hotel", buildSearchTab());
        tabs.addTab("Nuova recensione", buildReviewTab());
        tabs.addTab("Classifiche locali", buildRankingsTab());
        add(tabs, BorderLayout.CENTER);

        notifications.setEditable(false);
        JScrollPane notificationsPane = new JScrollPane(notifications);
        notificationsPane.setBorder(BorderFactory.createTitledBorder("Notifiche"));
        add(notificationsPane, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());
        logoutButton.addActionListener(e -> logout());
        password.addActionListener(e -> login());
        searchButton.addActionListener(e -> search());
        searchName.addActionListener(e -> search());
        searchCity.addActionListener(e -> search());
        reviewThisButton.addActionListener(e -> prepareReview());
        reviewButton.addActionListener(e -> sendReview());
        interestsButton.addActionListener(e -> setInterests());
        hotelsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedHotel();
            }
        });

        client.addNotificationListener(text -> SwingUtilities.invokeLater(() -> notify(text)));
        client.addRankingListener(() -> SwingUtilities.invokeLater(() -> {
            notify("Classifica locale aggiornata");
            fillRankings();
        }));
        updateState();
    }

    // ---------------------------------------------------------------- costruzione

    private JPanel buildTop() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        bar.add(new JLabel("Username"));
        bar.add(username);
        bar.add(new JLabel("Password"));
        bar.add(password);
        bar.add(loginButton);
        bar.add(registerButton);
        bar.add(logoutButton);

        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD));
        JPanel status = new JPanel(new BorderLayout());
        status.add(userLabel, BorderLayout.WEST);
        status.add(message, BorderLayout.CENTER);
        message.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        JPanel top = new JPanel(new BorderLayout());
        top.add(bar, BorderLayout.NORTH);
        top.add(status, BorderLayout.SOUTH);
        return top;
    }

    private JPanel buildSearchTab() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        form.add(new JLabel("Città"));
        form.add(searchCity);
        form.add(new JLabel("Nome hotel (vuoto = tutti gli hotel della città)"));
        form.add(searchName);
        form.add(searchButton);
        form.add(reviewThisButton);

        hotelsTable.setAutoCreateRowSorter(true);
        hotelsTable.setFillsViewportHeight(true);
        detail.setEditable(false);
        detail.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(hotelsTable), new JScrollPane(detail));
        split.setResizeWeight(0.6);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(form, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReviewTab() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        String[] labels = {"Punteggio globale", "Pulizia", "Posizione", "Servizi", "Qualità"};

        int row = 0;
        c.gridy = row++;
        form.add(new JLabel("Nome hotel"), c);
        c.gridx = 1;
        form.add(reviewHotel, c);
        c.gridx = 0;
        c.gridy = row++;
        form.add(new JLabel("Città"), c);
        c.gridx = 1;
        form.add(reviewCity, c);
        for (int i = 0; i < scores.length; i++) {
            scores[i] = named(new JSpinner(new SpinnerNumberModel(3, 0, 5, 1)), "score" + i);
            c.gridx = 0;
            c.gridy = row++;
            form.add(new JLabel(labels[i] + " (0-5)"), c);
            c.gridx = 1;
            form.add(scores[i], c);
        }
        c.gridx = 1;
        c.gridy = row;
        form.add(reviewButton, c);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(form);
        return panel;
    }

    private JPanel buildRankingsTab() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        form.add(new JLabel("Città (separate da virgola)"));
        form.add(interests);
        form.add(interestsButton);

        rankingsTable.setFillsViewportHeight(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(rankingsTable), BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------------- azioni

    public void register() {
        String user = username.getText().strip();
        String pass = new String(password.getPassword());
        background(() -> client.register(user, pass), result -> info(result.message(), result.ok()));
    }

    public void login() {
        String user = username.getText().strip();
        String pass = new String(password.getPassword());
        background(() -> client.login(user, pass), response -> {
            show(response);
            if (response instanceof Success) {
                password.setText("");
                refreshBadge();
            }
            updateState();
        });
    }

    public void logout() {
        background(client::logout, response -> {
            show(response);
            rankingsModel.setRowCount(0);
            interests.setText("");
            updateState();
        });
    }

    public void search() {
        String city = searchCity.getText().strip();
        String name = searchName.getText().strip();
        if (city.isEmpty()) {
            info("Inserisci la città.", false);
            return;
        }
        background(() -> name.isEmpty() ? client.searchCity(city) : client.searchHotel(name, city), this::show);
    }

    public void sendReview() {
        int[] values = Arrays.stream(scores).mapToInt(s -> (Integer) s.getValue()).toArray();
        String hotel = reviewHotel.getText().strip();
        String city = reviewCity.getText().strip();
        background(() -> client.insertReview(hotel, city, values[0],
                new Ratings(values[1], values[2], values[3], values[4])), response -> {
            show(response);
            if (response instanceof Success) {
                refreshBadge();
            }
        });
    }

    public void setInterests() {
        List<String> cities = Arrays.stream(interests.getText().split(","))
                .map(String::strip).filter(city -> !city.isEmpty()).toList();
        background(() -> {
            client.setInterests(cities);
            return cities;
        }, saved -> {
            info(saved.isEmpty() ? "Nessuna città di interesse." : "Città di interesse: " + String.join(", ", saved), true);
            fillRankings();
        });
    }

    private void prepareReview() {
        int row = hotelsTable.getSelectedRow();
        if (row < 0) {
            info("Seleziona un hotel nella tabella.", false);
            return;
        }
        Hotel hotel = shownHotels.get(hotelsTable.convertRowIndexToModel(row));
        reviewHotel.setText(hotel.name());
        reviewCity.setText(hotel.city());
        tabs.setSelectedIndex(1);
    }

    private void refreshBadge() {
        background(client::badge, response -> {
            if (response instanceof BadgeResult result) {
                userLabel.setText("Utente: " + client.username().orElse("?") + "  —  Badge: "
                        + result.badge().displayName());
            }
        });
    }

    // ---------------------------------------------------------------- visualizzazione

    private void show(Message response) {
        switch (response) {
            case Success r -> info(r.message(), true);
            case Failure r -> info(r.message(), false);
            case HotelResult r -> {
                fillHotels(List.of(r.hotel()));
                hotelsTable.setRowSelectionInterval(0, 0);
                info("Hotel trovato.", true);
            }
            case HotelListResult r -> {
                fillHotels(r.hotels());
                info(r.hotels().size() + " hotel trovati.", true);
            }
            case BadgeResult r -> info("Badge: " + r.badge().displayName(), true);
            case Request r -> info("Risposta inattesa dal server", false);
        }
    }

    private void fillHotels(List<Hotel> hotels) {
        shownHotels = List.copyOf(hotels);
        hotelsModel.setRowCount(0);
        for (Hotel hotel : hotels) {
            hotelsModel.addRow(new Object[]{hotel.name(), hotel.city(), round(hotel.rate()), hotel.reviewCount(),
                    round(hotel.rank()), hotel.localRank()});
        }
        detail.setText("");
    }

    private void showSelectedHotel() {
        int row = hotelsTable.getSelectedRow();
        detail.setText(row < 0 ? "" : Formatter.hotel(shownHotels.get(hotelsTable.convertRowIndexToModel(row))));
        detail.setCaretPosition(0);
    }

    private void fillRankings() {
        rankingsModel.setRowCount(0);
        List<CityRanking> rankings = client.rankings();
        for (CityRanking ranking : rankings) {
            for (Hotel hotel : ranking.hotels()) {
                rankingsModel.addRow(new Object[]{ranking.city(), hotel.localRank(), hotel.name(),
                        round(hotel.rate()), round(hotel.rank())});
            }
        }
    }

    private void updateState() {
        boolean loggedIn = client.isLoggedIn();
        if (!loggedIn) {
            userLabel.setText("Non collegato");
        } else if (userLabel.getText().startsWith("Non")) {
            userLabel.setText("Utente: " + client.username().orElse("?"));
        }
        loginButton.setEnabled(!loggedIn);
        registerButton.setEnabled(!loggedIn);
        username.setEnabled(!loggedIn);
        password.setEnabled(!loggedIn);
        logoutButton.setEnabled(loggedIn);
        reviewButton.setEnabled(loggedIn);
        interestsButton.setEnabled(loggedIn);
    }

    private void info(String text, boolean ok) {
        message.setForeground(ok ? OK_COLOR : ERROR_COLOR);
        message.setText(text);
    }

    private void notify(String text) {
        notifications.append("[" + LocalTime.now().withNano(0) + "] " + text + "\n");
        notifications.setCaretPosition(notifications.getDocument().getLength());
    }

    // ---------------------------------------------------------------- aiuti

    /** Esegue la richiesta in background e ne passa l'esito (nel thread Swing) a {@code onDone}. */
    private <T> void background(Callable<T> work, Consumer<T> onDone) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.call();
            }

            @Override
            protected void done() {
                try {
                    onDone.accept(get());
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause();
                    info(cause instanceof IOException
                            ? "Errore di connessione: impossibile comunicare con il server di Hotelier"
                            : "Errore: " + cause, false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static <T extends java.awt.Component> T named(T component, String name) {
        component.setName(name);
        return component;
    }

    private static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
