package LabInformatica;

/**
 * Tesista nidekka un tesista che vuole svolgere un lavoro su uno specifico computer del laboratorio
 */

public class Tesista extends Utente{
    private final int id_pc;

    /**
     *
     * @param idM matricola
     * @param t tutor
     * @param id_pc id del pc su cui il tesista deve lavorare
     */
    public Tesista(int idM, Tutor t, int id_pc){
        super(idM,t);
        this.id_pc = id_pc;
    }

    @Override
    void richiestaAccesso(){
        System.out.printf("Tesista %d ask for Pc : %d\n",this.getMatricola(),this.id_pc);
        tutor.texAccessRequest(this,id_pc);
        System.out.printf("Tesista %d have access to Pc : %d\n",this.getMatricola(),this.id_pc);
    }

    @Override
    void release(){
        System.out.printf("Tesista %d released Pc %d\n",this.getMatricola(),this.id_pc);
        tutor.texReleasePc(this,id_pc);
    }
}

