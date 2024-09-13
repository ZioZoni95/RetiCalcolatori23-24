package vo_reti;
/*Attivazione di threads: esempio slides lezione 1
Scrivere un programma che stampa le tabelline moltiplicative
dall' 1 al 10
    1.si attivino 10 threads
    2.ogni numero n, 1<= n<= 10,viene passato ad un thread
      diverso
    3.il task assegnato ad ogni thread consiste nello
      stampare la tabellina corrispondente al numero che gli è passato
      come parametro   
*/
import java.util.*;
import java.util.concurrent.*;

public class Calculator implements Runnable {
    private int number;
    public Calculator(int number){
        this.number=number;
    }
    public void run(){
        for (int i=1;i<=10;i++){
            System.out.printf("%s: %d * %d = %d\n",Thread.currentThread().getName(),number,i,i*number);
        }
    }
}
