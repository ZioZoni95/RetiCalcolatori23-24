package hotelier.client.ui;

import hotelier.client.HotelierClient;
import hotelier.client.ui.Command.*;
import hotelier.protocol.Message;
import hotelier.protocol.Message.Success;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.List;

/** Interfaccia a riga di comando del client. */
public final class PlainClientUi {

    private final HotelierClient client;
    private final BufferedReader in;
    private final PrintStream out;

    public static void run(HotelierClient client) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            new PlainClientUi(client, in, System.out).run();
        }
    }

    PlainClientUi(HotelierClient client, BufferedReader in, PrintStream out) {
        this.client = client;
        this.in = in;
        this.out = out;
        client.addNotificationListener(text -> out.println("\n<Notifica Ricevuta:> " + text + "\n"));
    }

    void run() throws IOException {
        out.println("Benvenuto su Hotelier, i migliori hotel a portata di click! "
                + "Si prega di digitare exit per uscire o help per il menù dei comandi\n");
        while (true) {
            out.print("Inserisci comando: ");
            out.flush();
            String line = in.readLine();
            if (line == null) {
                return;
            }
            if (line.isBlank()) {
                continue;
            }
            Command command;
            try {
                command = CommandParser.parse(line);
            } catch (CommandException e) {
                out.println("\n" + e.getMessage() + "\n");
                continue;
            }
            if (command instanceof Exit) {
                return;
            }
            try {
                out.println("\n" + execute(command) + "\n");
            } catch (IOException e) {
                out.println("\nErrore di connessione: impossibile comunicare con il server TCP di Hotelier");
                return;
            }
        }
    }

    private String execute(Command command) throws IOException {
        return switch (command) {
            case Help c -> Formatter.HELP;
            case Exit c -> "";
            case ShowLocalRanks c -> Formatter.localRankings(client.rankings());
            case Register c -> register(c);
            case Login c -> login(c);
            case Logout c -> Formatter.response(client.logout());
            case SearchHotel c -> Formatter.response(client.searchHotel(c.hotelName(), c.city()));
            case SearchAllHotels c -> Formatter.response(client.searchCity(c.city()));
            case InsertReview c -> Formatter.response(client.insertReview(c.hotelName(), c.city(), c.rate(), c.ratings()));
            case ShowMyBadges c -> Formatter.response(client.badge());
        };
    }

    private String register(Register command) {
        try {
            return client.register(command.username(), command.password()).message();
        } catch (RemoteException e) {
            return "Errore connessione RMI: impossibile contattare il server RMI di Hotelier";
        }
    }

    private String login(Login command) throws IOException {
        Message response = client.login(command.username(), command.password());
        String text = Formatter.response(response);
        if (response instanceof Success) {
            text += "\n" + askInterests();
        }
        return text;
    }

    /** Dopo il login chiede le città di interesse per le notifiche sulle classifiche locali. */
    private String askInterests() throws IOException {
        out.print("\nInserire le città di cui si ha particolare interesse (ognuna tra virgolette): ");
        out.flush();
        String line = in.readLine();
        try {
            List<String> cities = line == null ? List.of() : CommandParser.quotedArgs(line);
            if (cities.isEmpty()) {
                return "\n<Attenzione> Nessuna città di interesse è stata registrata.";
            }
            client.setInterests(cities);
            return "\nCittà di interesse registrate con successo!";
        } catch (CommandException e) {
            return "\n<Attenzione> " + e.getMessage() + ": nessuna città registrata.";
        } catch (RemoteException e) {
            return "\n<Attenzione> Impossibile registrare le città di interesse sul server RMI.";
        }
    }
}
