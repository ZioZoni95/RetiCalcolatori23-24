package Lez7_esPingPong;

/**
 * L'esercizio consiste nella scrittura di un server che offre il servizio di "Ping Pong" e del relativo programma client.
 * Un client invia al server un messaggio di "Ping".
 * - Il server, se riceve il messaggio, risponde con un messaggio di "Pong".
 * - Il client sta in attesa n secondi di ricevere il messaggio dal server (timeout) e poi termina.
 * Client e Server usano il protocollo UDP per lo scambio di messaggi.
 */

public class PongServerMain {
    public static void main (String[] args){
        PongServer server = new PongServer(5000, "localhost");
        server.start();
    }
}
