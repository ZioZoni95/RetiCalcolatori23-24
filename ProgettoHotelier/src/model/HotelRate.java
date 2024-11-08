package model;

/**
 * La classe HotelRate rappresenta le valutazioni specifiche di un hotel in base a vari criteri,
 * come la pulizia, la posizione, i servizi e la qualità complessiva.
 * Ogni istanza di questa classe contiene i punteggi relativi a questi aspetti.
 * Include metodi per ottenere e impostare questi valori, nonché un costruttore di copia per duplicare le valutazioni.
 */
public class HotelRate {

    private float cleaning;  // Punteggio relativo alla pulizia dell'hotel
    private float position;  // Punteggio relativo alla posizione dell'hotel
    private float services;  // Punteggio relativo ai servizi offerti dall'hotel
    private float quality;  // Punteggio relativo alla qualità complessiva dell'hotel

    // Costruttore senza parametri
    public HotelRate() {}

    /**
     * Costruttore parametrico per creare un oggetto HotelRate con valori specifici per ogni criterio.
     *
     * @param cleaning Punteggio relativo alla pulizia dell'hotel
     * @param position Punteggio relativo alla posizione dell'hotel
     * @param services Punteggio relativo ai servizi offerti
     * @param quality Punteggio relativo alla qualità complessiva dell'hotel
     */
    public HotelRate(float cleaning, float position, float services, float quality){
        this.cleaning = cleaning;
        this.position = position;
        this.services = services;
        this.quality = quality;
    }

    /**
     * Costruttore di copia per creare una nuova istanza di HotelRate a partire da un'altra esistente.
     *
     * @param rating L'istanza di HotelRate da copiare
     */
    public HotelRate(HotelRate rating){
        this.cleaning = rating.cleaning;
        this.position = rating.position;
        this.services = rating.services;
        this.quality = rating.quality;
    }

    // Getters and Setters per tutti i campi dell'oggetto HotelRate

    /**
     * Restituisce il punteggio relativo alla pulizia.
     *
     * @return Il punteggio di pulizia
     */
    public synchronized float getCleaning(){
        return cleaning;
    }

    /**
     * Imposta il punteggio relativo alla pulizia.
     *
     * @param cleaning Il nuovo punteggio di pulizia
     */
    public synchronized void setCleaning(float cleaning){
        this.cleaning = cleaning;
    }

    /**
     * Restituisce il punteggio relativo alla posizione dell'hotel.
     *
     * @return Il punteggio della posizione
     */
    public synchronized float getPosition(){
        return position;
    }

    /**
     * Imposta il punteggio relativo alla posizione.
     *
     * @param position Il nuovo punteggio della posizione
     */
    public synchronized void setPosition(float position){
        this.position = position;
    }

    /**
     * Restituisce il punteggio relativo ai servizi offerti dall'hotel.
     *
     * @return Il punteggio dei servizi
     */
    public synchronized float getServices(){
        return services;
    }

    /**
     * Imposta il punteggio relativo ai servizi.
     *
     * @param services Il nuovo punteggio dei servizi
     */
    public synchronized void setServices(float services){
        this.services = services;
    }

    /**
     * Restituisce il punteggio relativo alla qualità complessiva dell'hotel.
     *
     * @return Il punteggio della qualità
     */
    public synchronized float getQuality(){
        return quality;
    }

    /**
     * Imposta il punteggio relativo alla qualità complessiva.
     *
     * @param quality Il nuovo punteggio della qualità
     */
    public synchronized void setQuality(float quality){
        this.quality = quality;
    }

    /**
     * Metodo che fornisce una rappresentazione in formato stringa dell'oggetto HotelRate.
     *
     * @return La rappresentazione in stringa delle valutazioni dell'hotel
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("cleaning: ").append(cleaning).append("\n");
        builder.append("position: ").append(position).append("\n");
        builder.append("services: ").append(services).append("\n");
        builder.append("quality: ").append(quality);

        return builder.toString();  // Restituisce la rappresentazione in stringa
    }
}
