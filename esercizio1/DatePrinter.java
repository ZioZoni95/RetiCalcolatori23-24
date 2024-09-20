package esercizio1;
import java.util.Calendar;

/*Esercizio 1- Ciascun Programma ha un Thread
 Non creerai un Thread poichè la JVM crea sempre un main Thread
    1.Crea la classe DatePrinter con un metodo main s cui aggiungerai il codice per i passi seguenti
    2.Crea un loop infinito while(true)
    3.Nel corpo del loop devono essere eseguite le seguenti azioni: stampare data e ora correnti e nome del thread in
        esecuzione (usa Thread.currentThread()) e successivamente stare in sleep per 2 secondi.
        Usare java.util.Calendar per recuperare data e ora correnti.
 */
public class DatePrinter {
    public static void main(String [] args){
        while(true){
            System.out.println(Calendar.getInstance().getTime());
            System.out.println(Thread.currentThread().getName());
                try{
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    System.out.println("Sleep interrotta");
                    return;
                }
        }
    }
}
