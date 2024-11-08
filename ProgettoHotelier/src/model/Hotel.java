package model;

import java.util.ArrayList;
import java.util.List;

/**
 * La classe Hotel rappresenta un hotel con tutte le informazioni necessarie
 * per la gestione dei dettagli relativi a un hotel, come il nome, la descrizione,
 * la città, il numero di telefono, i servizi offerti, il punteggio medio delle recensioni,
 * le valutazioni specifiche (HotelRate), il numero di recensioni, il rank generale e il rank locale.
 * Questa classe include anche i metodi per la gestione di questi dati, come getter, setter e metodi di utilità.
 */
public class Hotel {

    private int id;  // Identificatore univoco dell'hotel
    private String name;  // Nome dell'hotel
    private String description;  // Descrizione dell'hotel
    private String city;  // Città in cui si trova l'hotel
    private String phone;  // Numero di telefono dell'hotel
    private List<String> services;  // Lista dei servizi offerti dall'hotel
    private float rate;  // Punteggio medio dell'hotel
    private HotelRate ratings;  // Oggetto che contiene le valutazioni specifiche per l'hotel
    private int reviewCount;  // Numero totale di recensioni ricevute
    private double rank;  // Rank complessivo dell'hotel (ranking globale)
    private int localRank;  // Rank dell'hotel a livello locale (della città)

    // Costruttore senza parametri per JSON Jackson (in particolare per le classi Astratte)
    public Hotel() {}

    /**
     * Costruttore parametrico per creare un oggetto Hotel.
     *
     * @param id L'ID univoco dell'hotel
     * @param name Il nome dell'hotel
     * @param description La descrizione dell'hotel
     * @param city La città in cui si trova l'hotel
     * @param phone Il numero di telefono dell'hotel
     * @param services La lista dei servizi offerti dall'hotel
     * @param rate Il punteggio medio dell'hotel
     * @param ratings Le valutazioni dettagliate dell'hotel
     */
    public Hotel(int id, String name, String description, String city, String phone, List<String> services, int rate,
                 HotelRate ratings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = new ArrayList<>(services);  // Crea una copia della lista dei servizi
        this.rate = rate;
        this.ratings = new HotelRate(ratings);  // Crea una nuova istanza di HotelRate
    }

    /**
     * Costruttore di copia per creare una nuova istanza di Hotel a partire da un altro oggetto Hotel.
     *
     * @param hotel L'istanza dell'hotel da copiare
     */
    public Hotel(Hotel hotel) {
        this.id = hotel.id;
        this.name = hotel.name;
        this.description = hotel.description;
        this.city = hotel.city;
        this.phone = hotel.phone;
        this.services = new ArrayList<>(hotel.services);  // Copia la lista dei servizi
        this.rate = hotel.rate;
        this.ratings = new HotelRate(hotel.ratings);  // Copia l'oggetto HotelRate
        this.reviewCount = hotel.reviewCount;
        this.rank = hotel.rank;
        this.localRank = hotel.localRank;
    }

    // Getters and Setters per tutti i campi dell'oggetto Hotel

    public int getId() {
        return id;  // Restituisce l'ID dell'hotel
    }

    public String getName() {
        return name;  // Restituisce il nome dell'hotel
    }

    public String getDescription() {
        return description;  // Restituisce la descrizione dell'hotel
    }

    public String getCity() {
        return city;  // Restituisce la città in cui si trova l'hotel
    }

    public String getPhone() {
        return phone;  // Restituisce il numero di telefono dell'hotel
    }

    public List<String> getServices() {
        return services;  // Restituisce la lista dei servizi offerti dall'hotel
    }

    /**
     * Restituisce il punteggio medio dell'hotel.
     *
     * @return Il punteggio medio dell'hotel
     */
    public synchronized float getRate() {
        return rate;
    }

    /**
     * Imposta il punteggio medio dell'hotel.
     *
     * @param rate Il nuovo punteggio medio da impostare
     */
    public synchronized void setRate(float rate) {
        this.rate = rate;
    }

    public synchronized HotelRate getRatings() {
        return ratings;  // Restituisce l'oggetto HotelRate con le valutazioni specifiche
    }

    public synchronized void setRatings(HotelRate ratings) {
        this.ratings = ratings;  // Imposta le valutazioni specifiche
    }

    public synchronized int getReviewCount() {
        return reviewCount;  // Restituisce il numero di recensioni ricevute
    }

    public synchronized void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;  // Imposta il numero di recensioni
    }

    /**
     * Incrementa il conteggio delle recensioni di uno.
     */
    public synchronized void incrementReviews() {
        this.reviewCount++;  // Incrementa il numero delle recensioni
    }

    public synchronized double getRank() {
        return rank;  // Restituisce il rank complessivo dell'hotel
    }

    /**
     * Imposta il rank complessivo dell'hotel.
     *
     * @param rank Il nuovo rank complessivo da impostare
     */
    public synchronized void setRankLevel(double rank) {
        this.rank = rank;
    }

    public synchronized void setLocalRanking(int localRank) {
        this.localRank = localRank;  // Imposta il rank locale (della città)
    }

    public synchronized int getLocalRank() {
        return localRank;  // Restituisce il rank locale
    }

    /**
     * Metodo che fornisce una rappresentazione in formato stringa dell'oggetto Hotel.
     *
     * @return La rappresentazione in stringa dell'hotel, contenente tutte le informazioni principali
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Nome: ").append(name).append("\n");
        sb.append("Descrizione: ").append(description).append("\n");
        sb.append("Città: ").append(city).append("\n");
        sb.append("Telefono: ").append(phone).append("\n");
        sb.append("Servizi: ").append(services).append("\n");
        sb.append("Punteggio: ").append(rate).append("\n");
        sb.append(ratings).append("\n");  // Aggiunge le informazioni di valutazione
        sb.append("Numero Recensioni: ").append(reviewCount).append("\n");
        sb.append("Rank: ").append(rank).append("\n");
        sb.append("Rank Locale: ").append(localRank);

        return sb.toString();  // Restituisce la rappresentazione in stringa
    }
}
