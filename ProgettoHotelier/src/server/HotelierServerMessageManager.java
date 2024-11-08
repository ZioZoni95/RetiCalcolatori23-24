package server;
import Response_Request_netPackets.*;
import Response_Request_netPackets.Request_ResponseMessage;
import Handlers.LoginHandlerUtente;
import model.Hotel;
import model.Recensioni;
import model.Utente;

import java.util.Collections;
import java.util.Comparator;

public class HotelierServerMessageManager {

    private Utente userClient;
    private final HotelierServerHotelManager hotelManager;
    private final HotelierServerUserManager userManager;
    private final HotelierServerReviewManager reviewManager;
    private final LoginHandlerUtente loginHandler;

    public HotelierServerMessageManager(){
        hotelManager = HotelierServerHotelManager.getInstance();
        userManager = HotelierServerUserManager.getInstance();
        reviewManager = HotelierServerReviewManager.getInstance();
        loginHandler = LoginHandlerUtente.getInstance();
    }

    // restituisce pacchetto di risposta in base al pacchetto passato come paramentro
    public Request_ResponseMessage handlePacket(Request_ResponseMessage packet) {

        // filtro rispetto a instanza del pacchetto passato
        return switch (packet) {
            case loginMessageRequest loginPacket -> handleLoginPacket((loginMessageRequest) packet);
            case logoutMessageRequest logoutPacket -> handleLogoutPacket((logoutMessageRequest) packet);
            case searchHotelMessageRequest hotelPacket -> handleHotelPacket((searchHotelMessageRequest) packet);
            case searchAllHotelsMessage hotelListPacket -> handleHotelListPacket((searchAllHotelsMessage) packet);
            case insertReviewRequestMessage reviewPacket -> handleReviewPacket((insertReviewRequestMessage) packet);
            case badgeLevelMessageRequest badgePacket -> handleBadgePacket((badgeLevelMessageRequest) packet);
            default -> packetErrorResponse("Errore: Pacchetto non supportato!");
        };
    }

    private errorResponseMessage packetErrorResponse(String message) {

        errorResponseMessage errorResponse = new errorResponseMessage("[ERRORE] " + message);
        return errorResponse;
    }

    // restitusce pacchetto di risposta login in caso di successo, pacchetto di errore in caso di fallimento
    private Request_ResponseMessage handleLoginPacket(loginMessageRequest packet) {

        // ottengo username e password dal pacchetto
        var username = packet.getUsername();
        var password = packet.getPassword();

        // controllo se utente ha già effettuato il login
        if (userClient != null) {
            // restituisco un pacchetto di errore in cui chiedo di fare il logout per effetter un nuovo login
            return packetErrorResponse("Login già effettuato per utente " + userClient.getUsername() + "! Eseguire logout per effetturare un nuovo login");
        }

        // controllo se esiste utente avente username passato
        if(userManager.getUserByName(username) == null) {
            // restituisco un pacchetto di errore in cui chiedo di effettuare la registrazione
            return packetErrorResponse("Utente non esiste, si prega di effettuare la registrazione!");
        }
        // recupero utente avente username e password passati
        var user = userManager.Authentication(username, password);
        // controllo che la password passata sia corretta
        if (user == null) {
            // restituisco un pacchetto di errore in cui notfico password errata
            return packetErrorResponse("Password errata!");
        }

        // controllo se è già attiva un sessione di login per user su un altro client
        if (loginHandler.userIsLogged(user)) {

            // restituisco un pacchetto di errore in cui notifico sessione già attiva per utente user su un altro client
            return packetErrorResponse("Sessione già attiva per utente " + user.getUsername() + " su un altro client");
        }
        // utente tovato e login non ancora effettuata
        // assegno a userClient utente trovato per memorizzarne il login senza dover tutte le volte passare dal loginHandler
        userClient = user;
        // aggiungo userClient alla lista di utenti loggati
        loginHandler.addLoggedUser(userClient);

        // restituisco un pacchetto di risposta in cui notifico il login avvenuto con successo
        loginResponseMessage packetLoginResponse = new loginResponseMessage("Login effettuato correttamente!");
        return packetLoginResponse;

    }

    // restitusce pacchetto di risposta logout in caso di successo, pacchetto di errore in caso di fallimento
    private Request_ResponseMessage handleLogoutPacket(logoutMessageRequest packet) {

        // controllo se utente non ha effettuato il login
        if (userClient == null) {
            // restituisco un pacchetto di errore in cui chiedo di effettuare il login per poter effettuare il logout
            return packetErrorResponse("Utente non loggato. Effettua il login prima di eseguire il logout.");
        }

        // utente loggato
        // rimuovo utente alla lista di utenti loggati
        loginHandler.removeUser(userClient);
        // Resetto userClient a null
        userClient = null;
        // restituisco un pacchetto di risposta in cui notifico il logout avvenuto con successo
        logoutResponseMessage packetLogoutResponse = new logoutResponseMessage("Logout effettuato correttamente!");
        return packetLogoutResponse;
    }

    // restitusce pacchetto di risposta hotel in caso di successo, pacchetto di errore in caso di fallimento
    private Request_ResponseMessage handleHotelPacket(searchHotelMessageRequest packet) {

        // ottengo nome e citta dell' hotel
        var hotelName = packet.getHotelName();
        var city = packet.getCity();
        // ottengo hotel avente nome e città passati
        var hotel = hotelManager.getHotelByNameAndCity(hotelName, city);
        // hotel non trovato
        if (hotel == null) {
            // restituisco un pacchetto di errore in cui notifico che l' hotel non esiste
            return packetErrorResponse("Hotel non trovato.");
        }

        //hotel trovato
        // restituisco un pacchetto di risposta contentente hotel richiesto
        searchHotelResponseMessage packetHotelResponse = new searchHotelResponseMessage(hotel);
        return packetHotelResponse;
    }

    // restitusce pacchetto di risposta hotelList in caso di successo, pacchetto di errore in caso di fallimento
    private Request_ResponseMessage handleHotelListPacket(searchAllHotelsMessage packet) {

        // ottengo città degli hotel
        var city = packet.getCity();
        // ottengo lista di hotel aventi città passata
        var hotels = hotelManager.getHotelsByCity(city);
        // nessun hotel trovato
        if (hotels.isEmpty()) {
            // restituisco un pacchetto di errore in cui notifico che non esiste nessun hotel per quella città
            return packetErrorResponse("Nessun hotel trovato.");
        }
        // hotel trovati
        // ordino lista di hotel in modo crescente rispetto al rank locale
        Collections.sort(hotels, Comparator.comparingInt(Hotel::getLocalRank));
        // restituisco un pacchetto di risposta contentente la lista di hotel ordinata
        searchAllHotelsResponseMessage packetHotelListResponse = new searchAllHotelsResponseMessage(hotels);
        return packetHotelListResponse;
    }

    // restitusce pacchetto di risposta review in caso di successo, pacchetto di errore in caso di fallimento
    private Request_ResponseMessage handleReviewPacket(insertReviewRequestMessage packet) {

        // controllo se utente non ha effettuato il login
        if (userClient == null) {
            // restituisco un pacchetto di errore in cui chiedo di effettuare il login per inserire una recensione
            return packetErrorResponse("Utente non loggato. Effettua il login per inserire una recensione.");
        }

        // utente loggato
        // ottengo nome hotel, citta , rate e rating dell' hotel
        var hotelName = packet.getHotelName();
        var city = packet.getCity();
        var rate = packet.getRate();
        var rating = packet.getRatings();

        // ottengo hotel avente nome e città passati
        var hotel = hotelManager.getHotelByNameAndCity(hotelName, city);
        // hotel non trovato
        if (hotel == null) {
            // restituisco un pacchetto di errore in cui notifico che l' hotel non esiste e quindi la recensione non è stata registrata
            return packetErrorResponse("Recensione non registata. Hotel non trovato");
        }

        // hotel trovato
        // creo una nuova recensione avente parametri passati
        var review = new Recensioni(userClient.getUsername(), hotel.getId(), rate, rating);
        // aggiungo la recensione alla lista di recensioni del registro
        reviewManager.addReview(review);
        // peristo la lista delle recensione del registro sul disco
        reviewManager.serialize();
        // incremento il numero di recensioni effettuate dall' utente userClient di 1
        userClient.incrementReviewCount();
        // controllo se è stato raggiunto un nuovo livello di esperienza e in caso setto il badge di utente userClient di conseguenza
        userClient.updateBadge();
        // peristo la lista degli utenti del registro sul disco
        userManager.serialize();
        // calcolo il nuovo rate medio
        updateHotelRate(hotel, review);
        // calcolo i nuovi punteggi medi: cleaning, position, servicese quality dell' hotel e li aggiorni
        updateHotelRating(hotel, review);
        // incremento il numero di recensioni relative all' hotel di 1
        hotel.incrementReviews();
        // Aggiorni il rate medio dell' hotel
        // peristo la lista degli hotel del registro sul disco
        hotelManager.serialize();

        // restituisco un pacchetto di risposta in cui notifico la registrazione della recensione avvenuto con successo
        insertReviewResponseMessage packetReviewResponse = new insertReviewResponseMessage("Recensione registrata con successo.");
        return packetReviewResponse;
    }

    // restitusce pacchetto di risposta badge in caso di successo, pacchetto di errore in caso di fallimento
    private Request_ResponseMessage handleBadgePacket(badgeLevelMessageRequest packet) {

        // controllo se utente non ha effettuato il login
        if (userClient == null) {
            // restituisco un pacchetto di errore in cui chiedo di effettuare il login per richieder il badge
            return packetErrorResponse("Utente non loggato. Effettua il login per richiedere il badge.");
        }
        // utente loggato
        // restituisco un pacchetto di risposta contentente badge dell' utente
        badgeLevelMessageResponse packetBadgeResponse = new badgeLevelMessageResponse(userClient.getBadgeLevel());
        return packetBadgeResponse;
    }

    // Metodo per gestire la disconnessione del client
    public void handleClientDisconnect() {

        // controllo se userClient era loggato
        if (userClient != null) {

            // rimuovo userClient dalla lista di utenti loggati
            loginHandler.removeUser(userClient);
            // resetto userClient a null
            userClient = null;
        }
    }

    // calcola il nuovo rate medio di hotel e lo aggiorna
    private void updateHotelRate(Hotel hotel, Recensioni review) {

        var reviewCount = hotel.getReviewCount();
        var avgRate = calculateNewAvg(reviewCount, hotel.getRate(), review.getRate());
        hotel.setRate(avgRate);
    }

    // calcola i nuovi punteggi medi di hotel e li aggiorna
    private void updateHotelRating(Hotel hotel, Recensioni review) {

        var hotelRating = hotel.getRatings();
        var reviewRating = review.getRating();
        var reviewCount = hotel.getReviewCount();

        var avgCleaning = calculateNewAvg(reviewCount, hotelRating.getCleaning(), reviewRating.getCleaning());
        hotelRating.setCleaning(avgCleaning);
        var avgPosition = calculateNewAvg(reviewCount, hotelRating.getPosition(), reviewRating.getPosition());
        hotelRating.setPosition(avgPosition);
        var avgServices = calculateNewAvg(reviewCount, hotelRating.getServices(), reviewRating.getServices());
        hotelRating.setServices(avgServices);
        var avgQuality = calculateNewAvg(reviewCount, hotelRating.getQuality(), reviewRating.getQuality());
        hotelRating.setQuality(avgQuality);
    }

    // restituisce nuovo valore medio arrotondato a una cifra decimale tramite ultimo valore medio, nuovo valore e numero di valori
    private float calculateNewAvg(int reviewCount, float avg, float value) {

        var totalScore = avg * reviewCount;
        var newAvg = (totalScore + value) / (reviewCount + 1);
        return Math.round(newAvg * 10.0f) / 10.0f;
    }


}
