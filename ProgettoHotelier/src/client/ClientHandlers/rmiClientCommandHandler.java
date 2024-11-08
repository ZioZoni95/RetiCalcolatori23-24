package client.ClientHandlers;

import RMI.RMIClient.HotelierClientRMI;
import client.HotelierCommands_Client;

import java.rmi.RemoteException;

public class rmiClientCommandHandler {
    private final HotelierClientRMI clientRMI;  // Riferimento al client RMI per comunicare con il server Hotelier

    /**
     * Costruttore per inizializzare l'handler con il client RMI.
     * @param clientRMI L'istanza del client RMI che si connetterà al server
     */
    public rmiClientCommandHandler(HotelierClientRMI clientRMI){
        this.clientRMI = clientRMI;  // Inizializza il riferimento al client RMI
    }

    /**
     * Gestisce il comando ricevuto, invocando il metodo appropriato in base al nome del comando.
     * @param command Il comando da eseguire, contenente nome e argomenti
     * @return La risposta in formato stringa del comando eseguito
     */
    public String handleCommand(HotelierCommands_Client command){
        String c_name = command.getName();  // Ottiene il nome del comando
        String[] c_args = command.getCommand_args();  // Ottiene gli argomenti del comando

        try {
            // Gestisce il comando in base al nome
            return switch (c_name){
                case "register" -> rmiHandleRegister(c_args);  // Se il comando è "register", gestisce la registrazione
                default -> null;  // Se il comando non è riconosciuto, restituisce null
            };
        } catch (RemoteException e) {
            // In caso di errore di connessione RMI, restituisce un messaggio di errore
            return "Errore connessione RMI: Impossibile contattare il server RMI di Hotelier";
        }
    }

    /**
     * Gestisce la registrazione di un nuovo utente.
     * @param c_args Gli argomenti del comando (username e password)
     * @return La risposta dal server RMI (successo o errore)
     * @throws RemoteException Se si verifica un errore nella comunicazione RMI
     */
    private String rmiHandleRegister(String[] c_args) throws RemoteException {
        String username = c_args[0];  // Ottiene il nome utente
        String password = c_args[1];  // Ottiene la password

        // Invia la richiesta di registrazione al server RMI e restituisce la risposta
        String response = clientRMI.requesteRegisterForNewUser(username, password);
        return response;  // Restituisce la risposta del server (successo o errore)
    }
}
