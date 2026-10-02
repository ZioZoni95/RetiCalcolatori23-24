package hotelier.client.ui;

import hotelier.model.CityRanking;
import hotelier.model.Hotel;
import hotelier.protocol.Message;
import hotelier.protocol.Message.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Testo mostrato all'utente. */
final class Formatter {

    private static final String SEPARATOR = "--------------------------------------------------";

    static final String HELP = """
            Menù dei Comandi disponibili:
            -- register "username" "password" - Registra un nuovo utente con username e password forniti
            -- login "username" "password" - Effettua il login con username e password forniti
            -- searchHotel "nomeHotel" "città" - Stampa l'hotel avente nome e città forniti
            -- searchAllHotels "città" - Stampa gli hotel della città fornita, ordinati per rank locale
            -- insertReview "nomeHotel" "città" "GlobalScore" "CleaningScore" "PositionScore" "ServicesScore" "QualityScore" - Inserisce una recensione (punteggi interi da 0 a 5)
            -- showMyBadges - Stampa il badge dell'utente corrispondente al livello raggiunto
            -- showLocalRanks - Stampa le classifiche delle città di interesse, ordinate per rank locale
            -- help - Stampa la lista di comandi disponibili
            -- logout - Effettua il logout
            -- exit - Termina il client""";

    private Formatter() {
    }

    static String response(Message response) {
        return switch (response) {
            case Success r -> r.message();
            case Failure r -> r.message();
            case HotelResult r -> hotel(r.hotel());
            case HotelListResult r -> hotels(r.hotels());
            case BadgeResult r -> "Badge: " + r.badge().displayName();
            case Request r -> "Risposta inattesa dal server";
        };
    }

    static String hotel(Hotel hotel) {
        var ratings = hotel.ratings();
        return String.join("\n",
                "Nome: " + hotel.name(),
                "Descrizione: " + hotel.description(),
                "Città: " + hotel.city(),
                "Telefono: " + hotel.phone(),
                "Servizi: " + String.join(", ", hotel.services()),
                "Punteggio: " + number(hotel.rate()),
                "Valutazioni: pulizia " + number(ratings.cleaning()) + ", posizione " + number(ratings.position())
                        + ", servizi " + number(ratings.services()) + ", qualità " + number(ratings.quality()),
                "Numero Recensioni: " + hotel.reviewCount(),
                "Rank: " + number(hotel.rank()),
                "Rank Locale: " + hotel.localRank());
    }

    static String hotels(List<Hotel> hotels) {
        return hotels.stream().map(Formatter::hotel).collect(Collectors.joining("\n" + SEPARATOR + "\n"));
    }

    static String localRankings(List<CityRanking> rankings) {
        if (rankings.isEmpty()) {
            return "Nessuna classifica locale disponibile: non sono state registrate città di interesse.";
        }
        return rankings.stream()
                .map(r -> "================ " + r.city() + " ================\n\n" + hotels(r.hotels()))
                .collect(Collectors.joining("\n\n"));
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
