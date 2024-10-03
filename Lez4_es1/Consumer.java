package Lez4_es1;

/**
 * Consumer modella un consumatore che "consuma" un numero in Dropbox
 * Il consumer può essere interessato ai numeri pari oppure (XOR) ai numeri dispari
 */
public class Consumer implements Runnable{
    private Dropbox dropbox;
    /**
     * even indica se il cunsumatore è interessato a numeri pari (true) o dispari (false)
     */
    private boolean even;

    /**
     * @param e true se il consumer è interessato a numeri pari, false altrimenti
     * @param d istanza classe Dropbox
     */
    public Consumer(boolean e, Dropbox d){
        even=e;
        dropbox=d;
    }
    @Override
    public void run (){
        while(true) {
            dropbox.take(even);
        }
    }
}
