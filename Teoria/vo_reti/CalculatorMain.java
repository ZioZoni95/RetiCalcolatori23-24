package vo_reti;

public class CalculatorMain {
    public static void main(String[] args){
        for (int i=1; i<=10; i++){
            Calculator calculator = new Calculator(i);
            Thread thread = new Thread(calculator);
            thread.run(); /*thread.start(); in questo caso il msg viene visulizzato
                             dopo l'esecuzione e non nel mezzo con start()
                             Il run() viene eseguito all'ìin*/
        }
        System.out.println("Avviato Calcolo Tabelline");

}
}

/*
 * Riepilogo: Per definire tasks ed attivare threds che li eseguano
 *  1.definire una classe R che implementi l'interfaccia Runnable,cioè implementare
 *    il metodo run. In questo modo si definisce un oggetto runnable, cioè 
 *    un task che può essere eseguito
 * 2.creare un'istanza T di R
 * 3.Per costruire il thread,utilizzare il costruttore
 *      public Thread(Runnable target)
 *   passando il task T come parametro
 * 4.avviare il thread con una start()
 */