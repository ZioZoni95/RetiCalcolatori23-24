package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Utente {
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
    @JsonProperty("reviewCount")
    private int reviewCount; // Numero di recensioni fatte dall'utente

    private UserBadge badgeLevel;

    // Costruttore di default richiesto da Jackson
    public Utente() {}

    public Utente(String username, String password) {
        this.username = username;
        this.password = password;
        reviewCount = 0; // Imposta il numero di recensioni iniziale a zero
        this.badgeLevel = UserBadge.Recensore;
    }

    // Getter e Setter
    public String getUsername() { return username; }
    //public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
 //   public void setPassword(String password) { this.password = password; }

    public synchronized UserBadge getBadgeLevel() { return badgeLevel; }
    public synchronized void setBadgeLevel(UserBadge badgeLevel) { this.badgeLevel = badgeLevel; }

    public int getReviewCount() { return reviewCount; }
    public synchronized void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    // Incrementa il numero di recensioni e aggiorna il distintivo
    public void incrementReviewCount() {
        this.reviewCount++;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Username: ").append(username).append("\n");
        builder.append("Password: ").append(password).append("\n");
        builder.append("Re Count: ").append(reviewCount).append("\n");
        builder.append("Badge Level: ").append(badgeLevel);

        return builder.toString();
    }

  /*  // Aggiorna il distintivo in base al numero di recensioni
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
*/
  // Esegue update del badge una volta raggiunta la soglia prestabilita
  public synchronized void updateBadge() {

      switch (reviewCount) {
          case 2:
              badgeLevel = UserBadge.Recensore_Esperto;

              break;
          case 3:
              badgeLevel = UserBadge.Contribuente;

              break;
          case 4:
              badgeLevel = UserBadge.Contribuente_Esperto;

              break;
          case 5:
              badgeLevel = UserBadge.Super_Contribuente;

              break;

          default:

              break;
      }
  }

    public enum UserBadge{
        Recensore, Recensore_Esperto, CONTRIBUENTE, Contribuente, Contribuente_Esperto, Super_Contribuente
    }
}
