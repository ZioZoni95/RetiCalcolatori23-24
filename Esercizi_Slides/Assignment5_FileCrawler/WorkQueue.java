package Assignment5_FileCrawler;
import java.util.*;

/**
 * WorkQueue modella una coda sincronizzata.
 */

public class WorkQueue {

    private LinkedList<String> list;
    /**
     * variabile bool con la seguente semantica
     *      true se non ci sono più item da aggiungere
     *     false altrimenti
     */
    private boolean done;

    /**
     *
     * numero di elementi presenti nella coda
     */
    private int size; //#direcotry in queue

    public WorkQueue(){
        list = new LinkedList<>();
        done = false;
        size = 0;
    }

    /**
     * Aggiunge un item alla coda
     *
     * @param s stringa da aggiungere alla coda
     */
    public synchronized void add(String s) {
        list.add(s);
        size++;
        notifyAll();
    }

    /**
     *
     * Preleva e ritorna un item della coda
     * @return un item se la coda non è vuota
     *          null se la coda  è vuota;
     */
    public synchronized  String remove(){
        String s;
        while (!done && size == 0){
            try{
                wait();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            };
        }
        if (size > 0){
            s = list.remove();
            size--;
        }
        else
            s = null;
        return s;
    }

    /**
     *
     * comunica che non ci sono più item da aggiungere alla coda
     */
    public synchronized void finish(){
        done = true;
        notifyAll();
    }
}
