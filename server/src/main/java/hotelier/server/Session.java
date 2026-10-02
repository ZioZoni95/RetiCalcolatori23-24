package hotelier.server;

import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.protocol.Message;
import hotelier.protocol.Message.*;

import java.util.List;

/** Stato e logica di una connessione client: gestisce le richieste e conosce l'utente collegato. */
public final class Session {

    private final Services services;
    private volatile String username;

    public Session(Services services) {
        this.services = services;
    }

    public Response handle(Message message) {
        return switch (message) {
            case LoginRequest r -> login(r);
            case LogoutRequest r -> logout();
            case SearchHotelRequest r -> searchHotel(r);
            case SearchCityRequest r -> searchCity(r);
            case InsertReviewRequest r -> insertReview(r);
            case BadgeRequest r -> badge();
            case Response r -> new Failure("[ERRORE] Messaggio non supportato");
        };
    }

    /** Da chiamare quando la connessione si chiude: libera l'utente eventualmente collegato. */
    public void close() {
        if (username != null) {
            logout();
        }
    }

    private Response login(LoginRequest request) {
        if (username != null) {
            return failure("Login già effettuato per utente " + username + "! Eseguire logout per effettuare un nuovo login");
        }
        if (isBlank(request.username()) || request.password() == null) {
            return failure("Username e password sono obbligatori");
        }
        var user = services.users().find(request.username());
        if (user.isEmpty()) {
            return failure("Utente non esiste, si prega di effettuare la registrazione!");
        }
        if (!services.users().passwordMatches(user.get(), request.password())) {
            services.events().log("Login fallito (password errata): " + user.get().username());
            return failure("Password errata!");
        }
        if (!services.sessions().tryLogin(user.get().username())) {
            return failure("Sessione già attiva per utente " + user.get().username() + " su un altro client");
        }
        username = user.get().username();
        services.events().log("Login: " + username);
        return new Success("Login effettuato correttamente!");
    }

    private Response logout() {
        String current = username;
        if (current == null) {
            return failure("Utente non loggato. Effettua il login prima di eseguire il logout.");
        }
        services.sessions().logout(current);
        username = null;
        services.events().log("Logout: " + current);
        return new Success("Logout effettuato correttamente, Arrivederci!");
    }

    private Response searchHotel(SearchHotelRequest request) {
        if (isBlank(request.hotelName()) || isBlank(request.city())) {
            return failure("Nome hotel e città sono obbligatori");
        }
        return services.hotels().findByNameAndCity(request.hotelName(), request.city())
                .<Response>map(HotelResult::new)
                .orElseGet(() -> failure("Hotel non trovato."));
    }

    private Response searchCity(SearchCityRequest request) {
        if (isBlank(request.city())) {
            return failure("La città è obbligatoria");
        }
        List<Hotel> hotels = services.hotels().findByCity(request.city());
        return hotels.isEmpty() ? failure("Nessun hotel trovato.") : new HotelListResult(hotels);
    }

    private Response insertReview(InsertReviewRequest request) {
        String current = username;
        if (current == null) {
            return failure("Utente non loggato. Effettua il login per inserire una recensione.");
        }
        Ratings ratings = request.ratings();
        if (isBlank(request.hotelName()) || isBlank(request.city()) || ratings == null) {
            return failure("Richiesta di recensione incompleta");
        }
        if (request.rate() < 0 || request.rate() > 5 || !ratings.isValid()) {
            return failure("I punteggi devono essere compresi tra 0 e 5");
        }
        var hotel = services.hotels().findByNameAndCity(request.hotelName(), request.city());
        if (hotel.isEmpty()) {
            return failure("Recensione non registrata. Hotel non trovato");
        }
        services.reviews().submit(current, hotel.get(), request.rate(), ratings);
        services.events().log("Recensione di " + current + " su " + hotel.get().name() + " ("
                + hotel.get().city() + "): " + request.rate() + "/5");
        return new Success("Recensione registrata con successo.");
    }

    private Response badge() {
        String current = username;
        if (current == null) {
            return failure("Utente non loggato. Effettua il login per richiedere il badge.");
        }
        return services.users().find(current)
                .<Response>map(u -> new BadgeResult(u.badgeLevel()))
                .orElseGet(() -> failure("Utente non trovato"));
    }

    private static Failure failure(String text) {
        return new Failure("[ERRORE] " + text);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
