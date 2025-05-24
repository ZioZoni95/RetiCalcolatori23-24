package Lez3Es1;

/*
*Scirvere un programma in cui un contatore viene aggiornato da "20 scrittori e il suo valore letto  e
* stampato da 20 lettori
*   --Definire un task Writer che implementa runnable e nel metodo run invoca il metodo increment di un ogeetto
*     Counter.*/

public class Writer implements Runnable {
    private Counter c; //riferimento ad un contatore istanza della classe Counter
    /**
     *
     * @param c contatore
      */
    public Writer(Counter c){
        this.c=c;
    }
    @Override
    public void run(){
        c.increment();
    }
}
