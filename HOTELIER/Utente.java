package HOTELIER;

/**
 * Queta classe modella gli utenti che si registrano e interagiscono con il sistema inservendo recensioni e
 * ottenendo distintivi in base al #recensioni fatte
 */
public class Utente {
    private final String username;
    private final String password;
    private String eMail;
    private int numRecensioni;
    private String rank; /*rank dell'utente basato sul num di Rencesioni*/

    //Costruttore
    public Utente (String username, String password, String eMail){
        this.username = username;
        this.eMail = eMail;
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

    //Getter per l'emeail
    public String geteMail(){
        return eMail;
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
