package model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * La classe Utente rappresenta un utente del sistema di recensioni. Ogni utente ha un nome utente, una password,
 * un numero di recensioni che ha scritto e un distintivo che rappresenta il livello di attività dell'utente.
 * L'oggetto Utente è utilizzato per memorizzare e gestire le informazioni relative a ciascun utente.
 */
public class Utente {
    @JsonProperty("username")
    private String username;  // Il nome utente dell'utente

    @JsonProperty("password")
    private String password;  // La password dell'utente

    @JsonProperty("reviewCount")
    private int reviewCount;  // Il numero di recensioni scritte dall'utente

    private UserBadge badgeLevel;  // Il distintivo (badge) che rappresenta il livello dell'utente

    // Costruttore di default richiesto da Jackson per la serializzazione/deserializzazione
    public Utente() {}

    /**
     * Costruttore per creare un utente con un nome utente e una password specificati.
     * Il numero di recensioni iniziale è impostato a 0 e il badge è impostato a "Recensore".
     *
     * @param username Il nome utente
     * @param password La password dell'utente
     */
    public Utente(String username, String password) {
        this.username = username;
        this.password = password;
        reviewCount = 0;  // Imposta il numero di recensioni iniziale a zero
        this.badgeLevel = UserBadge.Recensore;  // Imposta il livello iniziale del badge
    }

    // Getter e Setter

    public String getUsername() {
        return username;  // Restituisce il nome utente
    }

    public String getPassword() {
        return password;  // Restituisce la password dell'utente
    }

    // Restituisce il badge level dell'utente (distintivo)
    public synchronized UserBadge getBadgeLevel() {
        return badgeLevel;
    }

    // Imposta il badge level dell'utente
    public synchronized void setBadgeLevel(UserBadge badgeLevel) {
        this.badgeLevel = badgeLevel;
    }

    // Restituisce il numero di recensioni scritte dall'utente
    public int getReviewCount() {
        return reviewCount;
    }

    // Imposta il numero di recensioni scritte dall'utente
    public synchronized void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    /**
     * Incrementa il numero di recensioni dell'utente e aggiorna il distintivo se necessario.
     * Viene utilizzato per tenere traccia delle recensioni scritte e aggiornare il badge.
     */
    public synchronized void incrementReviewCount() {
        this.reviewCount++;
    }

    /**
     * Restituisce una rappresentazione stringa dell'utente.
     *
     * @return Una stringa che contiene tutte le informazioni relative all'utente
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Username: ").append(username).append("\n");
        builder.append("Password: ").append(password).append("\n");
        builder.append("ReviewCount: ").append(reviewCount).append("\n");
        builder.append("Badge Level: ").append(badgeLevel);

        return builder.toString();
    }

    /**
     * Aggiorna il livello del badge dell'utente in base al numero di recensioni scritte.
     * I badge sono assegnati in base a determinate soglie di recensioni.
     */
    public synchronized void updateBadge() {
        // Cambia il badge dell'utente in base al numero di recensioni
        switch (reviewCount) {
            case 2:
                badgeLevel = UserBadge.Recensore_Esperto;  // Badge per chi ha 2 recensioni
                break;
            case 3:
                badgeLevel = UserBadge.Contribuente;  // Badge per chi ha 3 recensioni
                break;
            case 4:
                badgeLevel = UserBadge.Contribuente_Esperto;  // Badge per chi ha 4 recensioni
                break;
            case 5:
                badgeLevel = UserBadge.Super_Contribuente;  // Badge per chi ha 5 recensioni
                break;
            default:
                break;  // Non viene modificato il badge per utenti con recensioni inferiori a 2
        }
    }

    /**
     * Enum che rappresenta i possibili livelli di badge di un utente.
     * I badge sono usati per premiare gli utenti in base al numero di recensioni scritte.
     */
    public enum UserBadge {
        Recensore,               // Badge per utenti con recensioni inferiori a 2
        Recensore_Esperto,       // Badge per utenti con 2 recensioni
        CONTRIBUENTE,            // Badge per utenti con 3 recensioni
        Contribuente,            // Badge per utenti con 4 recensioni
        Contribuente_Esperto,    // Badge per utenti con 5 recensioni
        Super_Contribuente       // Badge per utenti con recensioni superiori a 5
    }
}
