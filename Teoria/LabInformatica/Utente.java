package LabInformatica;
import java.util.Random;

/**
 * La Classe astratta utente modella un generico utente del Laboratorio*
 */

public abstract class Utente implements Runnable {
    private final int matricola;
    protected final  int k; //k accessi che l'utente deve fare
    protected long sleepTime;
    protected long workTime;
    protected Tutor tutor;

    /**
     *
     * @param m matricola
     * @param t tutor
     */
    public Utente (int m, Tutor t){
        this.tutor=t;
        this.matricola=m;
        Random rand = new Random();
        this.k= rand.nextInt(3) + 1;
        this.sleepTime = rand.nextInt(7500);
        this.workTime = rand.nextInt(5500);
    }
    @Override
    public void run(){
        try{
            for(int i=0; i<this.k;i++){
                //user ask access to tutor
                this.richiestaAccesso();
                Thread.sleep(this.workTime);
                //user left lab
                this.release();
                Thread.sleep(this.sleepTime);

            }
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }
    /**
     *
     * @return matricola utente
     */
    public int getMatricola(){
        return matricola;
    }

    /**
    * metodo per richiedere al tutor l'accesso
    */

    abstract void richiestaAccesso();

    /**
     *
     * @return Utente#k
     */

    public int getK(){
        return k;
    }
    /**
     *
     * release permette all'utente di comunicare al tutor di aver terminato il lavoro
     * e conseguentemente che può lasciare il laboratorio
     */
    abstract void release();
}
