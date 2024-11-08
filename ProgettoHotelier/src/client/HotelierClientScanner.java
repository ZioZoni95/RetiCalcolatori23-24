package client;

import RMI.RMIClient.HotelierClientRMI;
import client.ClientHandlers.ClientCommandHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;
import java.io.IOException;
import java.util.Arrays;

/**
 * La classe HotelierClientScanner è un componente del client Hotelier che gestisce l'interazione con l'utente attraverso la linea di comando (CLI).
 * Si occupa di:
 * 1. Leggere e analizzare i comandi inseriti dall'utente.
 * 2. Eseguire i comandi e stampare la risposta.
 * 3. Gestire la registrazione e deregistrazione per le callback RMI per le città di interesse.
 * 4. Gestire la connessione al gruppo multicast per ricevere notifiche sulle modifiche al rank locale delle città di interesse.
 *
 * La classe esegue queste operazioni all'interno di un thread separato per mantenere l'applicazione reattiva.
 */
public class HotelierClientScanner implements Runnable {
    private final HotelierClientRMI clientRMI; // Client RMI per la gestione delle operazioni remote
    private final ClientMulticast multicastReceiver; // Gestore per la connessione al gruppo multicast
    private final ClientCommandHandler cmd_Handler; // Gestore dei comandi inseriti dall'utente

    /**
     * Costruttore della classe HotelierClientScanner. Inizializza i componenti necessari per il funzionamento del client.
     *
     * @param clientRMI Il client RMI che gestisce le operazioni remote.
     * @param multicastReceiver Il ricevitore delle notifiche multicast.
     * @throws Exception Se si verifica un errore durante l'inizializzazione.
     */
    public HotelierClientScanner(HotelierClientRMI clientRMI, ClientMulticast multicastReceiver) throws Exception{
        this.clientRMI = clientRMI;
        this.multicastReceiver = multicastReceiver;
        cmd_Handler = new ClientCommandHandler(clientRMI);

        Thread thread = new Thread(this);
        thread.start(); // Avvio del thread per la gestione dei comandi in parallelo
    }

    /**
     * Metodo eseguito nel thread. Gestisce l'interazione continua con l'utente tramite la CLI.
     */
    @Override
    public void run(){
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Benvenuto su Hotelier, i migliori hotel a portata di click! Si prega di digitare exit per uscire o help per il meù dei comandi\n");

            while (!Thread.interrupted()) {
                System.out.print("Inserisci comando: ");
                String input = scanner.nextLine(); // Recupero dell'input inserito dall'utente

                if(input.isEmpty()) {
                    continue; // Se l'input è vuoto, continuo a chiedere il comando
                }

                // Parsing del comando inserito
                HotelierCommands_Client cmd = CommandParser.p_Command(input);
                if(cmd != null) {
                    if(StringUtils.equalsIgnoreCase(cmd.getName(),"exit")) {
                        close(); // Chiudo la connessione se il comando è "exit"
                        break; // Esco dal ciclo
                    }
                    try {
                        // Gestisco il comando e recupero la relativa risposta
                        String response = cmd_Handler.manageCommand(cmd);
                        // Stampo la risposta ricevuta
                        System.out.println("\n" + response);

                        // Controllo se è stato effettuato il login con successo
                        if (StringUtils.equals(response, "Login effettuato correttamente!")) {
                            // Se il login ha successo, chiedo all'utente di inserire città di interesse
                            System.out.print("\nInserire le città di cui si ha particolare interesse: ");
                            input = scanner.nextLine(); // Recupero l'input relativo alle città di interesse

                            // Parso le città inserite (devono essere passate tra virgolette)
                            String[] cities = StringUtils.substringsBetween(input, "\"", "\"");

                            // Verifico se l'utente ha inserito almeno una città di interesse
                            if (cities != null && cities.length != 0) {
                                // Registro il client per le callback RMI sui cambiamenti del rank locale
                                clientRMI.registerInterests(Arrays.asList(cities));
                                System.out.println("\nCittà di interesse registrate con successo !");
                            } else {
                                // Se non sono state inserite città, avviso l'utente
                                System.out.println("\n<Attenzione> Nessuna città di interesse è stata registrata.");
                            }

                            // Aggiungo il client al gruppo multicast per ricevere notifiche sui cambiamenti dei rank locali
                            multicastReceiver.joinGroup();
                        }

                        // Controllo se è stato effettuato il logout con successo
                        if (StringUtils.equals(response, "Logout effettuato correttamente, Arrivederci!")) {
                            // Se il logout ha successo, eseguo la deregistrazione per le callback RMI
                            if (!clientRMI.islocalRankMapEmpty()) {
                                // Deregistro il client per le callback RMI
                                clientRMI.removeInterest();
                                clientRMI.resetLocalRankMap();
                            }

                            // Rimuovo il client dal gruppo multicast per non ricevere più notifiche
                            multicastReceiver.leaveGroup();
                        }

                    } catch (IOException e) {
                        e.printStackTrace(); // Gestione delle eccezioni di I/O
                    }
                }

                System.out.println(""); // Stampa vuota per migliorare la leggibilità
            }
        }
    }

    /**
     * Metodo per chiudere tutte le connessioni attive e rilasciare le risorse.
     */
    private void close() {
        // Deregistro il client per le callback RMI e chiudo l'esportazione dello stub se necessario
        clientRMI.close();
        // Chiudo la connessione alla multicast socket se è ancora aperta
        multicastReceiver.close();
        var tcpHandler = cmd_Handler.getHotelierClientTcpHandler();
        // Chiudo la connessione TCP e gli stream associati
        tcpHandler.close();
    }
}
