package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Utente {
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
    @JsonProperty("badgeLevel")
    private String badgeLevel;
    @JsonProperty("reviewCount")
    private int reviewCount; // Numero di recensioni fatte dall'utente

    // Costruttore di default richiesto da Jackson
    public Utente() {}

    public Utente(String username, String password) {
        this.username = username;
        this.password = password;
        this.reviewCount = 0; // Imposta il numero di recensioni iniziale a zero
        this.badgeLevel = "Recensore"; // Livello iniziale di badge
    }

    // Getter e Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBadgeLevel() { return badgeLevel; }
    public void setBadgeLevel(String badgeLevel) { this.badgeLevel = badgeLevel; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    // Incrementa il numero di recensioni e aggiorna il distintivo
    public void incrementReviewCount() {
        this.reviewCount++;
        updateBadgeLevel();
    }

    // Aggiorna il distintivo in base al numero di recensioni
    private void updateBadgeLevel() {
        if (reviewCount >= 50) {
            badgeLevel = "Contributore Super";
        } else if (reviewCount >= 30) {
            badgeLevel = "Contributore Esperto";
        } else if (reviewCount >= 20) {
            badgeLevel = "Contributore";
        } else if (reviewCount >= 10) {
            badgeLevel = "Recensore Esperto";
        } else {
            badgeLevel = "Recensore";
        }
    }
}
