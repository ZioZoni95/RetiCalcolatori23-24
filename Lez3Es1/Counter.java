package Lez3Es1;
/*
*Scirvere un programma in cui un contatore viene aggiornato da "20 scrittori e il suo valore letto  e
* stampato da 20 lettori
*   --Creare una classe counter che offe i metodi increment e get() per incrementare
*     e recuperare il valore di un contatore.*/

public class Counter {
    private int counter = 0;

    public void increment(){
        counter++;
    }
    public int get(){
        return counter;
    }
}
