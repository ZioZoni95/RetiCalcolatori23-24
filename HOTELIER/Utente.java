package HOTELIER;

/**
 * Queta classe modella gli utenti che si registrano e interagiscono con il sistema inservendo recensioni e
 * ottenendo distintivi in base al #recensioni fatte
 */
public class Utente {
    private final String username;
    private final String password;
    private int numRecensioni;
    private String rank; /*rank dell'utente basato sul num di Rencesioni*/

    //Costruttore
    public Utente (String username, String password){
        this.username = username;
        this.password = password;
        this.numRecensioni = 0;
        this.rank = "Recensore";  //rank iniziale
    }

    //addRecensione per aggiungere recensione e aggiornare il rank
    public void addReview(){
        this.numRecensioni++;
        updateRank(); //Aggiorna il rank
    }

    public void updateRank(){
        if (numRecensioni >= 50){
            this.rank = "Contributo Super";
        }else if{
            thi
        }

    }
}
