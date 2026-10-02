package hotelier.client;

import hotelier.client.Command.*;
import hotelier.protocol.Message;
import hotelier.protocol.Message.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.util.List;

/** Interfaccia a riga di comando del client. */
final class Cli {

    private final TcpClient tcp;
    private final RmiClient rmi;
    private final MulticastListener multicast;
    private final BufferedReader in;
    private final PrintStream out;

    Cli(TcpClient tcp, RmiClient rmi, MulticastListener multicast, BufferedReader in, PrintStream out) {
        this.tcp = tcp;
        this.rmi = rmi;
        this.multicast = multicast;
        this.in = in;
        this.out = out;
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
            case ShowLocalRanks c -> Formatter.localRankings(rmi.rankings());
            case Register c -> register(c);
            case Login c -> login(c);
            case Logout c -> logout();
            case SearchHotel c -> Formatter.response(tcp.request(new SearchHotelRequest(c.hotelName(), c.city())));
            case SearchAllHotels c -> Formatter.response(tcp.request(new SearchCityRequest(c.city())));
            case InsertReview c ->
                    Formatter.response(tcp.request(new InsertReviewRequest(c.hotelName(), c.city(), c.rate(), c.ratings())));
            case ShowMyBadges c -> Formatter.response(tcp.request(new BadgeRequest()));
        };
    }

    private String register(Register command) {
        try {
            return rmi.register(command.username(), command.password()).message();
        } catch (RemoteException e) {
            return "Errore connessione RMI: impossibile contattare il server RMI di Hotelier";
        }
    }

    private String login(Login command) throws IOException {
        Message response = tcp.request(new LoginRequest(command.username(), command.password()));
        String text = Formatter.response(response);
        if (response instanceof Success) {
            text += "\n" + afterLogin();
        }
        return text;
    }

    /** Dopo il login: registra le città di interesse e si iscrive al gruppo multicast. */
    private String afterLogin() throws IOException {
        out.print("\nInserire le città di cui si ha particolare interesse (ognuna tra virgolette): ");
        out.flush();
        String line = in.readLine();
        StringBuilder result = new StringBuilder();
        try {
            List<String> cities = line == null ? List.of() : CommandParser.quotedArgs(line);
            if (cities.isEmpty()) {
                result.append("\n<Attenzione> Nessuna città di interesse è stata registrata.");
            } else {
                rmi.registerInterests(cities);
                result.append("\nCittà di interesse registrate con successo!");
            }
        } catch (CommandException e) {
            result.append("\n<Attenzione> ").append(e.getMessage()).append(": nessuna città registrata.");
        } catch (RemoteException e) {
            result.append("\n<Attenzione> Impossibile registrare le città di interesse sul server RMI.");
        }
        try {
            multicast.join();
        } catch (IOException e) {
            result.append("\n<Attenzione> Impossibile iscriversi alle notifiche multicast: ").append(e.getMessage());
        }
        return result.toString();
    }

    private String logout() throws IOException {
        Message response = tcp.request(new LogoutRequest());
        if (response instanceof Success) {
            try {
                rmi.unregisterInterests();
            } catch (RemoteException e) {
                // il server non è raggiungibile: la callback verrà rimossa da lui
            }
            multicast.leave();
        }
        return Formatter.response(response);
    }
}
