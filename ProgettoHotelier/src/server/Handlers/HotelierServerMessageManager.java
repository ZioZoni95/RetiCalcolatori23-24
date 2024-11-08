package server.Handlers;
import Response_Request_netPackets.*;
import Response_Request_netPackets.Request_ResponseMessage;
import model.Hotel;
import model.Recensioni;
import model.Utente;
import server.HotelierServerHotelManager;

import java.util.Collections;
import java.util.Comparator;

/**
 * Questa classe gestisce i pacchetti di messaggi ricevuti dal client e fornisce risposte
 * appropriate in base al tipo di richiesta. Si occupa di operazioni come login, logout,
 * ricerca di hotel, inserimento recensioni, e gestione dei badge.
 * Inoltre, gestisce la disconnessione dei client e l'aggiornamento dei dati degli hotel e degli utenti.
 */
public class HotelierServerMessageManager {

    private Utente userClient; // L'utente attualmente loggato
    private final HotelierServerHotelManager hotelManager; // Gestore degli hotel
    private final HotelierServerUserManager userManager; // Gestore degli utenti
    private final HotelierServerReviewManager reviewManager; // Gestore delle recensioni
    private final LoginHandlerUtente loginHandler; // Gestore delle sessioni di login

    /**
     * Costruttore della classe che inizializza i vari gestori per la gestione degli hotel,
     * degli utenti, delle recensioni e delle sessioni di login.
     */
    public HotelierServerMessageManager(){
        hotelManager = HotelierServerHotelManager.getInstance();
        userManager = HotelierServerUserManager.getInstance();
        reviewManager = HotelierServerReviewManager.getInstance();
        loginHandler = LoginHandlerUtente.getInstance();
    }

    /**
     * Gestisce il pacchetto di richiesta ricevuto, eseguendo operazioni specifiche in base
     * al tipo di pacchetto e restituendo la risposta appropriata.
     *
     * @param packet Il pacchetto di richiesta da gestire.
     * @return Il pacchetto di risposta da inviare al client.
     */
    public Request_ResponseMessage handlePacket(Request_ResponseMessage packet) {

        // Gestione del pacchetto in base al tipo specifico
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

    /**
     * Crea un pacchetto di risposta di errore con il messaggio passato.
     *
     * @param message Il messaggio di errore da includere nella risposta.
     * @return Il pacchetto di risposta di errore.
     */
    private errorResponseMessage packetErrorResponse(String message) {

        // Restituisce un pacchetto di risposta con il messaggio di errore fornito
        errorResponseMessage errorResponse = new errorResponseMessage("[ERRORE] " + message);
        return errorResponse;
    }

    /**
     * Gestisce la richiesta di login. Se il login ha successo, restituisce una risposta di successo,
     * altrimenti una risposta di errore.
     *
     * @param packet Il pacchetto di richiesta di login.
     * @return Il pacchetto di risposta con il risultato del login.
     */
    private Request_ResponseMessage handleLoginPacket(loginMessageRequest packet) {

        // Estrae username e password dal pacchetto di richiesta
        var username = packet.getUsername();
        var password = packet.getPassword();

        // Verifica se l'utente è già loggato
        if (userClient != null) {
            // Se l'utente è già loggato, restituisce un errore
            return packetErrorResponse("Login già effettuato per utente " + userClient.getUsername() + "! Eseguire logout per effetturare un nuovo login");
        }

        // Verifica che l'utente esista nel sistema
        if(userManager.getUserByName(username) == null) {
            // Se l'utente non esiste, restituisce un errore
            return packetErrorResponse("Utente non esiste, si prega di effettuare la registrazione!");
        }

        // Autenticazione dell'utente con username e password
        var user = userManager.Authentication(username, password);
        // Se la password è errata, restituisce un errore
        if (user == null) {
            return packetErrorResponse("Password errata!");
        }

        // Verifica che l'utente non sia già loggato su un altro client
        if (loginHandler.userIsLogged(user)) {
            return packetErrorResponse("Sessione già attiva per utente " + user.getUsername() + " su un altro client");
        }

        // Se l'autenticazione ha successo, registra l'utente come loggato
        userClient = user;
        loginHandler.addLoggedUser(userClient);

        // Restituisce una risposta di successo per il login
        loginResponseMessage packetLoginResponse = new loginResponseMessage("Login effettuato correttamente!");
        return packetLoginResponse;
    }

    /**
     * Gestisce la richiesta di logout. Se il logout ha successo, restituisce una risposta di successo,
     * altrimenti una risposta di errore.
     *
     * @param packet Il pacchetto di richiesta di logout.
     * @return Il pacchetto di risposta con il risultato del logout.
     */
    private Request_ResponseMessage handleLogoutPacket(logoutMessageRequest packet) {

        // Verifica se l'utente è loggato prima di eseguire il logout
        if (userClient == null) {
            return packetErrorResponse("Utente non loggato. Effettua il login prima di eseguire il logout.");
        }

        // Rimuove l'utente dalla lista degli utenti loggati e resetta l'utente
        loginHandler.removeUser(userClient);
        userClient = null;

        // Restituisce una risposta di successo per il logout
        logoutResponseMessage packetLogoutResponse = new logoutResponseMessage("Logout effettuato correttamente!");
        return packetLogoutResponse;
    }

    /**
     * Gestisce la richiesta di ricerca di un hotel. Restituisce l'hotel trovato o un messaggio di errore se non trovato.
     *
     * @param packet Il pacchetto di richiesta per la ricerca di un hotel.
     * @return Il pacchetto di risposta contenente l'hotel trovato o un errore.
     */
    private Request_ResponseMessage handleHotelPacket(searchHotelMessageRequest packet) {

        // Estrae nome e città dell'hotel dal pacchetto
        var hotelName = packet.getHotelName();
        var city = packet.getCity();

        // Cerca l'hotel con il nome e la città specificati
        var hotel = hotelManager.getHotelByNameAndCity(hotelName, city);
        if (hotel == null) {
            // Se l'hotel non esiste, restituisce un errore
            return packetErrorResponse("Hotel non trovato.");
        }

        // Se l'hotel è trovato, restituisce il pacchetto di risposta con i dettagli dell'hotel
        searchHotelResponseMessage packetHotelResponse = new searchHotelResponseMessage(hotel);
        return packetHotelResponse;
    }

    /**
     * Gestisce la richiesta di ricerca di tutti gli hotel in una città. Restituisce la lista degli hotel o un errore se nessun hotel è trovato.
     *
     * @param packet Il pacchetto di richiesta per la ricerca di tutti gli hotel in una città.
     * @return Il pacchetto di risposta con la lista degli hotel o un errore.
     */
    private Request_ResponseMessage handleHotelListPacket(searchAllHotelsMessage packet) {

        // Estrae la città degli hotel dal pacchetto
        var city = packet.getCity();

        // Ottiene la lista degli hotel nella città specificata
        var hotels = hotelManager.getHotelsByCity(city);
        if (hotels.isEmpty()) {
            // Se nessun hotel è trovato, restituisce un errore
            return packetErrorResponse("Nessun hotel trovato.");
        }

        // Ordina la lista degli hotel in base al ranking locale
        Collections.sort(hotels, Comparator.comparingInt(Hotel::getLocalRank));

        // Restituisce la lista di hotel ordinata
        searchAllHotelsResponseMessage packetHotelListResponse = new searchAllHotelsResponseMessage(hotels);
        return packetHotelListResponse;
    }

    /**
     * Gestisce la richiesta di inserimento di una recensione. Se la recensione è valida, aggiorna i dati dell'hotel
     * e dell'utente, altrimenti restituisce un errore.
     *
     * @param packet Il pacchetto contenente i dettagli della recensione.
     * @return Il pacchetto di risposta con il risultato dell'inserimento della recensione.
     */
    private Request_ResponseMessage handleReviewPacket(insertReviewRequestMessage packet) {

        // Verifica che l'utente sia loggato prima di inserire una recensione
        if (userClient == null) {
            return packetErrorResponse("Utente non loggato. Effettua il login per inserire una recensione.");
        }

        // Estrae i dettagli della recensione dal pacchetto
        var hotelName = packet.getHotelName();
        var city = packet.getCity();
        var rate = packet.getRate();
        var rating = packet.getRatings();

        // Cerca l'hotel specificato
        var hotel = hotelManager.getHotelByNameAndCity(hotelName, city);
        if (hotel == null) {
            return packetErrorResponse("Recensione non registrata. Hotel non trovato");
        }

        // Crea una nuova recensione
        var review = new Recensioni(userClient.getUsername(), hotel.getId(), rate, rating);
        // Aggiunge la recensione e la persiste
        reviewManager.addReview(review);
        reviewManager.serialize();

        // Incrementa il numero di recensioni dell'utente e aggiorna il suo badge
        userClient.incrementReviewCount();
        userClient.updateBadge();
        userManager.serialize();

        // Aggiorna il punteggio medio dell'hotel
        updateHotelRate(hotel, review);
        updateHotelRating(hotel, review);

        // Incrementa il numero di recensioni per l'hotel
        hotel.incrementReviews();
        hotelManager.serialize();

        // Restituisce una risposta di successo per la recensione
        insertReviewResponseMessage packetReviewResponse = new insertReviewResponseMessage("Recensione registrata con successo.");
        return packetReviewResponse;
    }

    /**
     * Gestisce la richiesta per ottenere il badge dell'utente. Se l'utente è loggato, restituisce il suo badge.
     *
     * @param packet Il pacchetto di richiesta per ottenere il badge dell'utente.
     * @return Il pacchetto di risposta con il badge dell'utente.
     */
    private Request_ResponseMessage handleBadgePacket(badgeLevelMessageRequest packet) {

        // Verifica che l'utente sia loggato prima di restituire il badge
        if (userClient == null) {
            return packetErrorResponse("Utente non loggato. Effettua il login per richiedere il badge.");
        }

        // Restituisce il badge dell'utente loggato
        badgeLevelMessageResponse packetBadgeResponse = new badgeLevelMessageResponse(userClient.getBadgeLevel());
        return packetBadgeResponse;
    }

    /**
     * Gestisce la disconnessione del client, rimuovendo l'utente dalla lista degli utenti loggati.
     */
    public void handleClientDisconnect() {

        // Verifica se l'utente era loggato prima di procedere
        if (userClient != null) {
            loginHandler.removeUser(userClient);
            userClient = null;
        }
    }

    /**
     * Calcola il nuovo punteggio medio di una recensione per l'hotel e lo aggiorna.
     *
     * @param hotel L'hotel da aggiornare.
     * @param review La recensione da considerare per il calcolo del nuovo punteggio medio.
     */
    private void updateHotelRate(Hotel hotel, Recensioni review) {

        var reviewCount = hotel.getReviewCount();
        var avgRate = calculateNewAvg(reviewCount, hotel.getRate(), review.getRate());
        hotel.setRate(avgRate);
    }

    /**
     * Calcola i nuovi punteggi medi di un hotel in base alle recensioni e aggiorna i valori.
     *
     * @param hotel L'hotel da aggiornare.
     * @param review La recensione da considerare per il calcolo dei nuovi punteggi medi.
     */
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

    /**
     * Calcola il nuovo punteggio medio dato il numero di recensioni, il punteggio attuale e il nuovo valore da aggiungere.
     *
     * @param reviewCount Il numero totale di recensioni.
     * @param avg Il punteggio medio attuale.
     * @param value Il nuovo punteggio da aggiungere.
     * @return Il nuovo punteggio medio, arrotondato a una cifra decimale.
     */
    private float calculateNewAvg(int reviewCount, float avg, float value) {

        var totalScore = avg * reviewCount;
        var newAvg = (totalScore + value) / (reviewCount + 1);
        return Math.round(newAvg * 10.0f) / 10.0f;
    }
}
