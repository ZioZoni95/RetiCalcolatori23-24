package RMI.RMIServer;

import RMI.RMIClient.HotelierClientInterface;
import server.Handlers.HotelierServerUserManager;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * La classe `HotelierRMIServerImp` implementa l'interfaccia `HotelierServerInterface` e gestisce la logica del server RMI
 * per la registrazione degli utenti e la gestione delle callback dai client.
 * Essa mantiene una mappa di callback per ciascun client, in cui ogni client è associato a una lista di città di interesse.
 * La classe consente di registrare e deregistrare client per le callback relative alle città,
 * nonché di gestire la registrazione degli utenti per il sistema.
 */
public class HotelierRMIServerImp implements HotelierServerInterface {

    /**
     * Mappa che memorizza le callback dei client.
     * La chiave è l'interfaccia `HotelierClientInterface` che rappresenta il client RMI,
     * mentre il valore è una lista di città di interesse per ciascun client.
     */
    private final Map<HotelierClientInterface, List<String>> clientCallback;

    /**
     * Oggetto `HotelierServerUserManager` che gestisce la registrazione e l'autenticazione degli utenti.
     * È un'istanza singleton utilizzata per gestire gli utenti nel sistema.
     */
    private HotelierServerUserManager usersAuthenticated;

    /**
     * Costruttore della classe. Inizializza la mappa delle callback e ottiene l'istanza del gestore degli utenti.
     * Viene creato un oggetto `HotelierServerUserManager` per gestire la registrazione degli utenti.
     * Inoltre, viene inizializzata la mappa `clientCallback` per tenere traccia delle città di interesse per ciascun client.
     */
    public HotelierRMIServerImp(){
        usersAuthenticated = HotelierServerUserManager.getInstance();  // Inizializza il gestore degli utenti.
        clientCallback = new HashMap<>();  // Inizializza la mappa delle callback dei client.
    }

    /**
     * Registra un client RMI e la lista delle città di interesse per ricevere callback in caso di aggiornamenti
     * sui ranghi locali degli hotel.
     * La mappa `clientCallback` tiene traccia di quali client sono interessati a quali città, associando il client
     * alla lista delle città di interesse.
     *
     * @param callbackClient L'interfaccia del client che ha effettuato la registrazione per ricevere le callback.
     * @param cities La lista delle città di interesse per il client.
     * @throws RemoteException Se si verifica un errore durante l'interazione con il client RMI.
     */
    @Override
    public synchronized void registerCallback(HotelierClientInterface callbackClient, List<String> cities) throws RemoteException {
        // Aggiunge il client e la relativa lista di città di interesse alla mappa delle callback
        clientCallback.put(callbackClient, cities);
    }

    /**
     * Registra un nuovo utente nel sistema, fornendo un nome utente e una password.
     * Il metodo invoca il gestore degli utenti per registrare l'utente e restituisce il risultato della registrazione.
     *
     * @param username Il nome utente da registrare.
     * @param password La password associata al nome utente.
     * @return Una stringa contenente il risultato della registrazione (ad esempio, un messaggio di successo o errore).
     * @throws RemoteException Se si verifica un errore durante la registrazione dell'utente.
     */
    @Override
    public String registerUser(String username, String password) throws RemoteException {
        return usersAuthenticated.register(username, password);  // Registra l'utente tramite il gestore.
    }

    /**
     * Deregistra un client RMI e rimuove la sua registrazione per le callback relative alle città di interesse.
     * Una volta deregistrato, il client non riceverà più notifiche sui cambiamenti dei ranghi locali.
     *
     * @param callbackClient L'interfaccia del client da deregistrare.
     * @throws RemoteException Se si verifica un errore durante la deregistrazione del client.
     */
    @Override
    public synchronized void unregisterCallback(HotelierClientInterface callbackClient) throws RemoteException {
        // Rimuove il client dalla mappa delle callback, cessando le sue registrazioni per le città di interesse
        clientCallback.remove(callbackClient);
    }

    /**
     * Restituisce la mappa delle callback registrate, che associa ogni client alla lista delle città di interesse.
     * Questa mappa può essere utilizzata per vedere quali client sono registrati per ricevere aggiornamenti su quali città.
     *
     * @return La mappa che associa ciascun client alla lista delle città di interesse.
     */
    public synchronized Map<HotelierClientInterface, List<String>> getClientsCallback() {
        return clientCallback;  // Restituisce la mappa delle callback registrate.
    }
}
