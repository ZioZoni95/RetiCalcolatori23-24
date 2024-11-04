package RMI;

import Handlers.HandlerUtente;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementazione del server RMI per la gestione degli utenti.
 * Utilizza HandlerUtente per la persistenza degli utenti e tiene traccia
 * degli utenti loggati.
 */
public class HotelierServiceImpl extends UnicastRemoteObject implements HotelierService {
    private HandlerUtente userHandler; // Riferimento al gestore degli utenti (persistenza)
    private Set<String> loggedUsers;   // Set per tenere traccia degli utenti loggati

    // Costruttore che accetta un'istanza di HandlerUtente
    public HotelierServiceImpl(HandlerUtente userHandler) throws RemoteException {
        super();
        this.userHandler = userHandler;
        this.loggedUsers = new HashSet<>();
    }

    /**
     * Registra un nuovo utente.
     * @param username nome utente
     * @param password password dell'utente
     * @return messaggio di feedback sulla registrazione
     * @throws RemoteException se si verifica un problema di comunicazione RMI
     */
    @Override
    public synchronized String register(String username, String password) throws RemoteException {
        return userHandler.registerUser(username, password); // Registra l'utente usando HandlerUtente
    }

    /**
     * Effettua il login di un utente.
     * @param username nome utente
     * @param password password dell'utente
     * @return messaggio di feedback sul login
     * @throws RemoteException se si verifica un problema di comunicazione RMI
     */
    @Override
    public synchronized String login(String username, String password) throws RemoteException {
        if (loggedUsers.contains(username)) {
            return "Errore: Utente già loggato";
        }
        String loginResult = userHandler.validateUserLogin(username, password);
        if (loginResult.equals("Login completato")) {
            loggedUsers.add(username); // Aggiungi l'utente all'elenco dei loggati
        }
        return loginResult;
    }

    /**
     * Effettua il logout dell'utente.
     * @param username nome utente
     * @return messaggio di feedback sul logout
     * @throws RemoteException se si verifica un problema di comunicazione RMI
     */
    @Override
    public synchronized String logout(String username) throws RemoteException {
        if (loggedUsers.remove(username)) {
            return "Logout completato";
        } else {
            return "Errore: Utente non loggato";
        }
    }
}
