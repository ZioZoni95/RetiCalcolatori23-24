package LabInformatica;

/**
 * Professor modella un professore che vuole svolgere attivitò didattichie per le quali è
 * richiesto l'utilizzo dell'intero laboratorio
 */
public class Professor extends Utente {
    /**
     * @param idP identificativo professore
     * @param idT identificativo Tutor
     */
    public Professor(int idP, Tutor idT) {
        super(idP, idT);
    }

    @Override
    void richiestaAccesso(){
        System.out.printf("Professor %d ask for access request\n",this.getMatricola());
        tutor.profAceessReq(this);
    }

    @Override
    void release(){
        System.out.printf("Professor free the lab\n",this.getMatricola());
        tutor.profReleaseLab(this);
    }
}

