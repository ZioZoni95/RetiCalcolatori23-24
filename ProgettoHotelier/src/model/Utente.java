package model;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Queta classe modella gli utenti che si registrano e interagiscono con il sistema inservendo recensioni e
 * ottenendo distintivi in base al #recensioni fatte
 */
public class Utente implements Serializable{
    @JsonProperty("username")
    private final String username;

    @JsonProperty("password")
    private final String password;
  //  private String eMail;

    @JsonProperty("numRecensioni")
    private int numRecensioni;

    @JsonProperty("rank")
    private String rank; /*rank dell'utente basato sul num di Rencesioni*/

    //Costruttore
    public Utente (String username, String password){
        this.username = username;
       // this.eMail = eMail;
        this.password = password;
        numRecensioni = 0;
        this.rank = "Recensore";  //rank iniziale
    }

    //addRecensione per aggiungere recensione e aggiornare il rank
    public void addReview(){
        this.numRecensioni++;
        updateRank(); //Aggiorna il rank
    }

    public void updateRank(){
        if (numRecensioni >= 50) {
            this.rank = "Contributore Super";
        }
        else if(numRecensioni >= 30){
            this.rank = "Contributore Esperto";
        }else if(numRecensioni >= 15){
            this.rank = "Contributore";
        }else if (numRecensioni >= 5){
            this.rank = "Recensore Esperto";
        }else{
            this.rank ="Recensore";
        }
    }

    //Getter per l'username
    public String getUsername(){
        return username;
    }

    //Getter per il numRecensioni
    public int getNumRecensioni() {
        return numRecensioni;
    }

    public String getRank(){
        return rank;
    }


    //Verifica della password (da testare)
    public boolean checkPassword(String password){
        return this.password.equals(password);
    }
}
