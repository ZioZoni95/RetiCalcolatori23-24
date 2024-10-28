/*
scrivere una applicazione JAVA che
● crea e attiva n thread.
● ogni thread esegue esattamente lo stesso task, ovvero conta il numero di
interi minori di 10,000,000 che sono primi
●
il numero di thread che devono essere attivati e mandati in esecuzione
viene richiesto all’utente, che lo inserisce tramite la CLI (Command Line
Interface)
● analizzare come varia il tempo di esecuzione dei thread attivati a seconda del
loro numero
●
sviluppare quindi un programma in cui si creano n task, tutti eseguono la
computazione descritta in precedenza e vengono sottomessi a un threadpool
di dimensione prefissata
 */

import java.util.Scanner;

public class PrimeC {
    private final static int MAX = 10_000_000;

    public static void main(String[] args) {
        int nThread = 0;
        Scanner sc = new Scanner(System.in);
        System.out.print("numero di thread tra 1 e 30: ");
        nThread = sc.nextInt();
        while (nThread < 1 || nThread > 30) {
            if (nThread < 1 || nThread > 30) {
                System.out.println("INVALID");
            }
            nThread = sc.nextInt();
        }
        System.out.println("\nCreating " + nThread + " prime counting threads...");
        ThreadCounterPrime[] worker = new ThreadCounterPrime[nThread];
        for (int i = 0; i < nThread; i++) {
            worker[i] = new ThreadCounterPrime(i, MAX);
        }
        for (int i = 0; i < nThread; i++) {
            worker[i].start();
        }
        System.out.println("Thread creati e avviati");
        sc.close();
    }
}