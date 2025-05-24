package Lez4_es1;
import java.util.Random;

/**
 *
 * Producer modella un produttore che inserisce un numero in Dropbox
 */
public class Producer implements Runnable{
    private final Dropbox dropbox;

    /**
     *
     * @param d istanza dela classe Dropbox
     */
    public Producer(Dropbox d){
        dropbox=d;
    }
    @Override
    public void run(){
        Random rand= new Random();
        while(true){
            int n = rand.nextInt(1000);
            dropbox.put(n);
        }
    }
}
