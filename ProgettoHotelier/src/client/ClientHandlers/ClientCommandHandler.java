package client.ClientHandlers;

import RMI.RMIClient.HotelierClientRMI;
import client.HotelierCommands_Client;

import java.io.IOException;

/**
 * La classe ClientCommandHandler si occupa della gestione dei comandi passati
 * I comandi vengono suddivisi in base alla tipologia di comando:
 *      1. Comando TCP per la gestione dei comandi TCP: insertReview, searchHotels, searchAllHotels, login, logout
 *      2. Comando RMI per la gestione dei comandi RMI: register
 *      3. Comando LOCAL: gestisce i comandi help e showLocalRanks
 */
public class ClientCommandHandler {

    // Riferimenti agli handler per i vari tipi di comando
    private final HotelierClientRMI clientRMI;  // Handler per i comandi RMI
    private final TCPCommandHandlerClient tcpComm_Handler;  // Handler per i comandi TCP
    private final rmiClientCommandHandler rmiCommHandler;  // Handler per i comandi RMI specifici

    // Costruttore: inizializza gli handler
    public ClientCommandHandler(HotelierClientRMI clientRMI) throws Exception {
        this.clientRMI = clientRMI;  // RMI handler passato come parametro
        tcpComm_Handler = new TCPCommandHandlerClient();  // Crea l'istanza per i comandi TCP
        rmiCommHandler = new rmiClientCommandHandler(clientRMI);  // Crea l'istanza per i comandi RMI
    }

    /**
     * Gestisce il comando ricevuto, invocando l'handler appropriato in base al tipo di comando
     * @param cmd Il comando da eseguire
     * @return La risposta in formato stringa del comando eseguito
     * @throws IOException Se si verifica un errore durante l'elaborazione del comando
     */
    public String manageCommand(HotelierCommands_Client cmd) throws IOException {
        // Gestisce il comando in base al tipo di comando (TCP, RMI, LOCAL)
        return switch(cmd.getC_type()){
            case COMMAND_TCP -> tcpComm_Handler.TCPCommandHandler(cmd);  // Se comando TCP, usa l'handler TCP
            case COMMAND_RMI -> rmiCommHandler.handleCommand(cmd);  // Se comando RMI, usa l'handler RMI
            case COMMAND_LOCAL -> handleLocalCMD(cmd);  // Se comando LOCAL, gestisce il comando in locale
            default -> null;  // Se il comando non è riconosciuto, restituisce null
        };
    }

    /**
     * Gestisce i comandi di tipo LOCAL
     * @param cmd Il comando da eseguire
     * @return La risposta del comando eseguito (può essere null se il comando non è valido)
     */
    private String handleLocalCMD(HotelierCommands_Client cmd) {
        // Gestisce i comandi locali come "help" e "showlocalranks"
        return switch (cmd.getName()){
            case "help" -> handleHelpCMD(cmd);  // Se il comando è "help", invoca la gestione del comando help
            case "showlocalranks" -> handleShowLocalRanks(cmd);  // Se il comando è "showlocalranks", invoca la gestione dei ranking locali
            default -> null;  // Se il comando non è riconosciuto, restituisce null
        };
    }

    /**
     * Gestisce il comando "help" che stampa la lista di comandi disponibili
     * @param cmd Il comando da eseguire (in questo caso, il comando help)
     * @return La lista di comandi disponibili come stringa
     */
    private String handleHelpCMD(HotelierCommands_Client cmd) {
        StringBuilder response = new StringBuilder();
        response.append("Menù dei Comandi disponibili:\n");
        // Elenco dei comandi supportati dal client
        response.append("-- register \"username\" \"password\" - Registra un nuovo utente con username e password forniti\n");
        response.append("-- login \"username\" \"password\" - Effettua il login con username e password forniti\n");
        response.append("-- searchHotel \"nomeHotel\" \"città\" - Stampa hotel avente nome e città forniti\n");
        response.append("-- searchAllHotels \"città\" - Stampa tutti lista di hotel situati nella città fornita, ordinati per rank locale\n");
        response.append("-- insertReview \"nomeHotel\" \"nomeCittà\" \"GlobalScore\" \"CleaningScore\" \"PositionScore\" \"ServicesScore\" \"QualityScore\" - Inserisce una recensione per hotel avente parametri forniti\n");
        response.append("-- showMyBadges - Stampa il badge dell'utente corrispondente al livello raggiunto\n");
        response.append("-- showLocalRanks - Stampa le liste di hotel delle città di interesse, ordinate per rank locale\n");
        response.append("-- help - Stampa la lista di comandi disponibili\n");
        response.append("-- logout - Effettua il logout\n");
        response.append("-- exit - Termina il client");

        return response.toString();  // Restituisce la lista di comandi disponibili
    }

    /**
     * Gestisce il comando "showlocalranks", che restituisce una lista di hotel ordinati per rank locale
     * @param cmd Il comando da eseguire
     * @return La lista di hotel ordinata per rank locale come stringa
     */
    private String handleShowLocalRanks(HotelierCommands_Client cmd) {
        // Utilizza l'oggetto RMI per ottenere la lista degli hotel ordinata per rank locale
        return clientRMI.localRankMapToString();  // Restituisce la lista di hotel ordinata
    }

    /**
     * Restituisce l'istanza dell'handler dei comandi TCP
     * @return L'istanza dell'handler per i comandi TCP
     */
    public TCPCommandHandlerClient getHotelierClientTcpHandler() {
        return tcpComm_Handler;
    }
}
