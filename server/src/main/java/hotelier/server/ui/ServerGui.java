package hotelier.server.ui;

import hotelier.config.ServerConfig;
import hotelier.model.Hotel;
import hotelier.model.User;
import hotelier.server.EventLog;
import hotelier.server.ServerRuntime;
import hotelier.util.JsonStore;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

/** Interfaccia grafica del server: configurazione, avvio/arresto, statistiche, utenti, hotel e log. */
public final class ServerGui extends JPanel {

    private final Path dataDir;
    private ServerRuntime server;

    private final JSpinner tcpPort = port(9999);
    private final JSpinner rmiPort = port(1099);
    private final JSpinner mcastPort = port(49152);
    private final JSpinner rankingInterval = plain(new JSpinner(new SpinnerNumberModel(10, 1, 86_400, 1)));
    private final JTextField address = new JTextField(10);
    private final JTextField rmiReference = new JTextField(14);
    private final JTextField mcastAddress = new JTextField(10);
    private final JButton startStop = new JButton("Avvia server");
    private final JButton rankNow = new JButton("Ricalcola ranking");
    private final JLabel statusLabel = new JLabel("Server fermo");

    private final JLabel[] statValues = new JLabel[7];
    private final JTextArea loggedInList = new JTextArea(6, 20);
    private final DefaultTableModel usersModel = new DefaultTableModel(
            new String[]{"Utente", "Recensioni", "Badge", "Online"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel hotelsModel = new DefaultTableModel(
            new String[]{"ID", "Nome", "Città", "Punteggio", "Recensioni", "Rank", "Rank locale"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return switch (column) {
                case 0, 4, 6 -> Integer.class;
                case 3, 5 -> Double.class;
                default -> String.class;
            };
        }
    };
    private final JTextArea logArea = new JTextArea();
    private final JTabbedPane tabs = new JTabbedPane();
    private final Consumer<EventLog.Entry> eventListener =
            entry -> SwingUtilities.invokeLater(() -> appendLog(entry.toString()));
    private final Timer refreshTimer = new Timer(1000, e -> refresh());

    public static void run(Path dataDir) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // si usa il look and feel di default
            }
            ServerGui gui = new ServerGui(dataDir);
            JFrame frame = new JFrame("Hotelier Server");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    gui.shutdown();
                    frame.dispose();
                    System.exit(0);
                }
            });
            frame.setContentPane(gui);
            frame.setSize(900, 640);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public ServerGui(Path dataDir) {
        super(new BorderLayout(8, 8));
        this.dataDir = dataDir;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        loadConfig();

        add(buildTop(), BorderLayout.NORTH);
        tabs.addTab("Dashboard", buildDashboard());
        tabs.addTab("Utenti", scroll(sortableTable(usersModel)));
        tabs.addTab("Hotel", scroll(sortableTable(hotelsModel)));
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tabs.addTab("Log", new JScrollPane(logArea));
        tabs.addChangeListener(e -> refresh());
        add(tabs, BorderLayout.CENTER);

        startStop.addActionListener(e -> toggle());
        rankNow.addActionListener(e -> {
            if (server != null) {
                server.rankNow();
            }
        });
        refresh();
        refreshTimer.start();
    }

    private JPanel buildTop() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Configurazione"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.anchor = GridBagConstraints.WEST;
        Object[][] rows = {
                {"Indirizzo", address, "Porta TCP", tcpPort, "Porta RMI", rmiPort},
                {"Riferimento RMI", rmiReference, "Gruppo multicast", mcastAddress, "Porta multicast", mcastPort},
                {"Intervallo ranking (s)", rankingInterval}
        };
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length; col++) {
                c.gridx = col;
                c.gridy = row;
                Object item = rows[row][col];
                form.add(item instanceof String text ? new JLabel(text) : (java.awt.Component) item, c);
            }
        }

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(startStop);
        controls.add(rankNow);
        controls.add(statusLabel);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(controls, BorderLayout.SOUTH);
        return top;
    }

    private JPanel buildDashboard() {
        String[] names = {"Stato", "Client connessi", "Utenti collegati", "Utenti registrati", "Hotel", "Recensioni",
                "Uptime"};
        JPanel stats = new JPanel(new GridBagLayout());
        stats.setBorder(BorderFactory.createTitledBorder("Statistiche"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 8, 4, 8);
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < names.length; i++) {
            statValues[i] = new JLabel("-");
            statValues[i].setFont(statValues[i].getFont().deriveFont(Font.BOLD));
            c.gridy = i;
            c.gridx = 0;
            stats.add(new JLabel(names[i] + ":"), c);
            c.gridx = 1;
            stats.add(statValues[i], c);
        }
        loggedInList.setEditable(false);
        JScrollPane online = new JScrollPane(loggedInList);
        online.setBorder(BorderFactory.createTitledBorder("Utenti collegati"));
        online.setPreferredSize(new Dimension(220, 150));

        JPanel statsColumn = new JPanel(new BorderLayout());
        statsColumn.add(stats, BorderLayout.NORTH);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(statsColumn, BorderLayout.WEST);
        panel.add(online, BorderLayout.CENTER);
        return panel;
    }

    private static JTable sortableTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        return table;
    }

    private static JScrollPane scroll(JTable table) {
        return new JScrollPane(table);
    }

    private static JSpinner port(int value) {
        return plain(new JSpinner(new SpinnerNumberModel(value, 1, 65_535, 1)));
    }

    /** Numeri senza separatore delle migliaia (le porte si scrivono 9999, non 9,999). */
    private static JSpinner plain(JSpinner spinner) {
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
        return spinner;
    }

    private void loadConfig() {
        ServerConfig config;
        try {
            config = JsonStore.readOrCreate(dataDir.resolve("ServerConfig.json"), ServerConfig.class,
                    ServerConfig.defaults());
        } catch (Exception e) {
            config = ServerConfig.defaults();
        }
        address.setText(config.serverAddress());
        tcpPort.setValue(config.tcpPort());
        rmiPort.setValue(config.rmiPort());
        rmiReference.setText(config.rmiRemoteReference());
        mcastAddress.setText(config.mcastAddress());
        mcastPort.setValue(config.mcastPort());
        rankingInterval.setValue(config.rankingInterval());
    }

    private ServerConfig readForm() {
        return new ServerConfig((Integer) tcpPort.getValue(), (Integer) rmiPort.getValue(),
                (Integer) mcastPort.getValue(), (Integer) rankingInterval.getValue(), address.getText().trim(),
                rmiReference.getText().trim(), mcastAddress.getText().trim());
    }

    private void toggle() {
        if (server == null) {
            start();
            if (server == null) {
                return;
            }
        } else {
            stop();
        }
    }

    /** Avvia il server con la configurazione del form (che viene anche salvata su disco). */
    public void start() {
        if (server != null) {
            return;
        }
        ServerConfig config = readForm();
        try {
            JsonStore.write(dataDir.resolve("ServerConfig.json"), config);
            server = ServerRuntime.start(dataDir, config);
            logArea.setText("");
            server.events().recent().forEach(entry -> appendLog(entry.toString()));
            server.events().addListener(eventListener);
        } catch (Exception e) {
            server = null;
            appendLog("[ERRORE] Impossibile avviare il server: " + e);
            if (isShowing()) {
                JOptionPane.showMessageDialog(this, "Impossibile avviare il server:\n" + e.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
        refresh();
    }

    public void stop() {
        if (server != null) {
            server.events().removeListener(eventListener);
            server.close();
            appendLog("Server fermato");
            server = null;
        }
        refresh();
    }

    /** Ferma il server e il timer di aggiornamento: da chiamare alla chiusura della finestra. */
    public void shutdown() {
        refreshTimer.stop();
        stop();
    }

    public boolean isRunning() {
        return server != null;
    }

    /** Aggiorna stato, statistiche e la tabella visibile. */
    public void refresh() {
        boolean running = server != null;
        startStop.setText(running ? "Ferma server" : "Avvia server");
        rankNow.setEnabled(running);
        for (JSpinner spinner : List.of(tcpPort, rmiPort, mcastPort, rankingInterval)) {
            spinner.setEnabled(!running);
        }
        for (JTextField field : List.of(address, rmiReference, mcastAddress)) {
            field.setEnabled(!running);
        }
        statusLabel.setText(running ? "● Server in esecuzione" : "■ Server fermo");

        if (!running) {
            for (JLabel value : statValues) {
                value.setText("-");
            }
            statValues[0].setText("Fermo");
            loggedInList.setText("");
            return;
        }
        List<String> online = server.loggedInUsers();
        statValues[0].setText("In esecuzione");
        statValues[1].setText(String.valueOf(server.connectedClients()));
        statValues[2].setText(String.valueOf(online.size()));
        statValues[3].setText(String.valueOf(server.users().size()));
        statValues[4].setText(String.valueOf(server.hotels().size()));
        statValues[5].setText(String.valueOf(server.reviewCount()));
        statValues[6].setText(format(server.uptime()));
        loggedInList.setText(String.join("\n", online));

        switch (tabs.getSelectedIndex()) {
            case 1 -> fillUsers(online);
            case 2 -> fillHotels();
            default -> {
            }
        }
    }

    private void fillUsers(List<String> online) {
        Set<String> onlineKeys = new java.util.HashSet<>();
        online.forEach(name -> onlineKeys.add(name.toLowerCase(Locale.ROOT)));
        usersModel.setRowCount(0);
        for (User user : server.users()) {
            usersModel.addRow(new Object[]{user.username(), user.reviewCount(), user.badgeLevel().displayName(),
                    onlineKeys.contains(user.username().toLowerCase(Locale.ROOT)) ? "sì" : ""});
        }
    }

    private void fillHotels() {
        hotelsModel.setRowCount(0);
        for (Hotel hotel : server.hotels()) {
            hotelsModel.addRow(new Object[]{hotel.id(), hotel.name(), hotel.city(),
                    round(hotel.rate()), hotel.reviewCount(), round(hotel.rank()), hotel.localRank()});
        }
    }

    private void appendLog(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }

    private static String format(Duration duration) {
        return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
    }
}
