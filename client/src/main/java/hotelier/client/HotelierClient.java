package hotelier.client;

import hotelier.config.ClientConfig;
import hotelier.model.CityRanking;
import hotelier.model.Ratings;
import hotelier.protocol.Message;
import hotelier.protocol.Message.*;
import hotelier.util.Result;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Il client Hotelier, senza interfaccia utente: unisce TCP (richieste), RMI (registrazione e
 * classifiche locali) e multicast (notifiche). Condiviso da CLI, TUI e GUI.
 * Le richieste TCP sono bloccanti e vanno quindi eseguite fuori dal thread dell'interfaccia.
 */
public final class HotelierClient implements Closeable {

    private final TcpClient tcp;
    private final RmiClient rmi;
    private final MulticastListener multicast;
    private final List<Consumer<String>> notificationListeners = new CopyOnWriteArrayList<>();
    private volatile String username;

    /** Si connette al server descritto dalla configurazione (TCP, RMI e gruppo multicast). */
    public static HotelierClient connect(ClientConfig config) throws Exception {
        TcpClient tcp = null;
        RmiClient rmi = null;
        try {
            tcp = new TcpClient(config.serverAddress(), config.tcpPort());
            rmi = new RmiClient(config.serverAddress(), config.rmiPort(), config.rmiRemoteReference());
            return new HotelierClient(tcp, rmi, config);
        } catch (Exception e) {
            if (rmi != null) {
                rmi.close();
            }
            if (tcp != null) {
                tcp.close();
            }
            throw e;
        }
    }

    private HotelierClient(TcpClient tcp, RmiClient rmi, ClientConfig config) throws IOException {
        this.tcp = tcp;
        this.rmi = rmi;
        this.multicast = new MulticastListener(config.mcastAddress(), config.mcastPort(), this::notifyListeners);
    }

    /** Utente collegato, se c'è. */
    public Optional<String> username() {
        return Optional.ofNullable(username);
    }

    public boolean isLoggedIn() {
        return username != null;
    }

    public Result register(String username, String password) throws RemoteException {
        return rmi.register(username, password);
    }

    /** Dopo un login riuscito ci si iscrive al gruppo multicast delle notifiche. */
    public Message login(String username, String password) throws IOException {
        Message response = tcp.request(new LoginRequest(username, password));
        if (response instanceof Success) {
            this.username = username;
            try {
                multicast.join();
            } catch (IOException e) {
                notifyListeners("<Attenzione> Impossibile iscriversi alle notifiche multicast: " + e.getMessage());
            }
        }
        return response;
    }

    /** Dopo un logout riuscito si lasciano città di interesse e gruppo multicast. */
    public Message logout() throws IOException {
        Message response = tcp.request(new LogoutRequest());
        if (response instanceof Success) {
            this.username = null;
            try {
                rmi.clearInterests();
            } catch (RemoteException e) {
                // il server non è raggiungibile: sarà lui a rimuovere la callback
            }
            multicast.leave();
        }
        return response;
    }

    public Message searchHotel(String hotelName, String city) throws IOException {
        return tcp.request(new SearchHotelRequest(hotelName, city));
    }

    public Message searchCity(String city) throws IOException {
        return tcp.request(new SearchCityRequest(city));
    }

    public Message insertReview(String hotelName, String city, int rate, Ratings ratings) throws IOException {
        return tcp.request(new InsertReviewRequest(hotelName, city, rate, ratings));
    }

    public Message badge() throws IOException {
        return tcp.request(new BadgeRequest());
    }

    /** Sostituisce le città di interesse; un elenco vuoto le rimuove tutte. */
    public void setInterests(List<String> cities) throws RemoteException {
        if (cities.isEmpty()) {
            rmi.clearInterests();
        } else {
            rmi.setInterests(cities);
        }
    }

    public List<String> interests() {
        return rmi.interests();
    }

    /** Ultime classifiche ricevute per le città di interesse, in ordine alfabetico di città. */
    public List<CityRanking> rankings() {
        return rmi.rankings();
    }

    /** Invocato (da un thread RMI) quando arriva una classifica aggiornata. */
    public void addRankingListener(Runnable listener) {
        rmi.addRankingListener(listener);
    }

    /** Invocato (da un thread dedicato) per ogni notifica multicast o avviso del client. */
    public void addNotificationListener(Consumer<String> listener) {
        notificationListeners.add(listener);
    }

    private void notifyListeners(String text) {
        notificationListeners.forEach(listener -> listener.accept(text));
    }

    @Override
    public void close() {
        multicast.close();
        rmi.close();
        tcp.close();
    }
}
