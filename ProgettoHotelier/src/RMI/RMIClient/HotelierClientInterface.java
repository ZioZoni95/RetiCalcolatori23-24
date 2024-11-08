package RMI.RMIClient;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Questa interfaccia definisce un contratto che il client RMI deve implementare per ricevere notifiche
 * riguardanti l'interesse in un hotel, in particolare un aggiornamento sul rango locale dell'hotel.
 * L'interfaccia estende l'interfaccia `Remote` di Java RMI per consentire la comunicazione remota.
 */
public interface HotelierClientInterface extends Remote {

    /**
     * Metodo per notificare l'interesse dell'utente, aggiornando il rango locale di un hotel.
     * Questo metodo deve essere implementato dal client RMI e invocato dal server RMI.
     *
     * @param serializedLocalRank Una stringa serializzata che rappresenta il rango locale dell'hotel.
     *                            Il rango è una misura della posizione di un hotel all'interno di una città
     *                            o di una località specifica.
     * @throws RemoteException Se si verifica un errore durante la comunicazione remota.
     */
    public void notifyInterest(String serializedLocalRank) throws RemoteException;
}
