package RMI.RMIServer;

import RMI.RMIClient.HotelierClientInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interfaccia `HotelierServerInterface` che definisce i metodi esposti dal server RMI per la gestione delle operazioni
 * relative agli utenti e alle callback dei client. L'interfaccia permette di registrare utenti, registrare e deregistrare
 * callback per le città di interesse, e gestire la comunicazione tra il server e i client RMI.
 */
public interface HotelierServerInterface extends Remote {

    /**
     * Metodo per registrare un nuovo utente nel sistema. Il server utilizza questo metodo per aggiungere un nuovo utente
     * e associare un nome utente e una password a quest'ultimo. Il risultato del processo di registrazione è restituito come stringa.
     *
     * @param username Il nome utente da registrare.
     * @param password La password associata al nome utente.
     * @return Una stringa che indica il risultato della registrazione (ad esempio, successo o errore).
     * @throws RemoteException Se si verifica un errore durante l'interazione con il server RMI.
     */
    public String registerUser(String username, String password) throws RemoteException;

    /**
     * Metodo che registra un client RMI per ricevere callback in caso di aggiornamenti sui ranghi locali degli hotel.
     * Il client specifica le città di interesse e viene aggiunto alla lista di callback per tali città.
     * Il server invierà notifiche ai client quando ci sono cambiamenti nei ranghi locali degli hotel per le città di interesse.
     *
     * @param clientCallback L'interfaccia del client che si è registrato per ricevere le callback.
     * @param city La lista delle città di interesse per il client.
     * @throws RemoteException Se si verifica un errore durante l'interazione con il server RMI.
     */
    public void registerCallback(HotelierClientInterface clientCallback, List<String> city) throws RemoteException;

    /**
     * Metodo che deregistra un client RMI dalle callback relative alle città di interesse. Quando un client si deregistra,
     * non riceverà più notifiche sugli aggiornamenti dei ranghi locali per le città di interesse precedentemente registrate.
     *
     * @param callbackClient L'interfaccia del client da deregistrare dalle callback.
     * @throws RemoteException Se si verifica un errore durante l'interazione con il server RMI.
     */
    public void unregisterCallback(HotelierClientInterface callbackClient) throws RemoteException;
}
