package Lez3Es1;
/*
*Scirvere un programma in cui un contatore viene aggiornato da "20 scrittori e il suo valore letto  e
* stampato da 20 lettori
    --Definire un task Reader che implementa Runnable e nel metodo run invoca il metodo get di un oggetto
      Counter e lo stampa
 */
public class Reader implements Runnable {
    private Counter c;

    public Reader(Counter c){
        this.c=c;
    }

    @Override
    public void run(){
        System.out.println(c.get());
    }
}
