package model;

import java.time.LocalDateTime;

/**
 * La classe Recensioni rappresenta una recensione fatta da un utente su un hotel.
 * Ogni recensione contiene il nome dell'utente che ha scritto la recensione, l'ID dell'hotel, il punteggio assegnato,
 * una valutazione dettagliata basata su vari criteri (es. pulizia, posizione, servizi, qualità), e la data e l'ora in cui è stata pubblicata la recensione.
 */
public class Recensioni {
    private String username;  // Il nome dell'utente che ha scritto la recensione
    private int hotelID;  // L'ID dell'hotel a cui si riferisce la recensione
    private int rate;  // Il punteggio complessivo assegnato alla recensione
    private HotelRate rating;  // La valutazione dettagliata (pulizia, posizione, servizi, qualità)
    private String timestamp;  // Data e ora in cui è stata scritta la recensione

    // Costruttore senza parametri
    public Recensioni(){}

    /**
     * Costruttore che inizializza i dati relativi alla recensione, compreso il timestamp della creazione.
     *
     * @param username Il nome dell'utente che ha scritto la recensione
     * @param hotelID L'ID dell'hotel recensito
     * @param rate Il punteggio complessivo dato alla recensione
     * @param rating Un oggetto HotelRate contenente la valutazione dettagliata
     */
    public Recensioni(String username, int hotelID, int rate, HotelRate rating){
        this.username = username;
        this.hotelID = hotelID;
        this.rate = rate;
        this.rating = new HotelRate(rating);  // Crea una copia della valutazione

        // Ottiene la data e l'ora di quando è stata messa la recensione
        timestamp = LocalDateTime.now().toString();  // Utilizza LocalDateTime per ottenere la data e l'ora correnti
    }

    // Getter per ottenere il nome dell'utente che ha scritto la recensione
    public String getUsername() {
        return username;
    }

    // Getter per ottenere l'ID dell'hotel recensito
    public int gethotelID() {
        return hotelID;
    }

    // Getter per ottenere il punteggio complessivo della recensione
    public int getRate() {
        return rate;
    }

    // Getter per ottenere la valutazione dettagliata (HotelRate)
    public HotelRate getRating() {
        return new HotelRate(rating);  // Restituisce una copia della valutazione per evitare modifiche esterne
    }

    // Getter per ottenere il timestamp della recensione (data e ora)
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Restituisce una rappresentazione stringa della recensione.
     *
     * @return Una stringa contenente tutte le informazioni relative alla recensione
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Username: ").append(username).append("\n");
        builder.append("HotelID: ").append(hotelID).append("\n");
        builder.append("Rate: ").append(rate).append("\n");
        builder.append("Review: ").append(rating).append("\n");
        builder.append("Timestamp: ").append(timestamp);

        return builder.toString();
    }

}
