package RMI.RMIClient;

import RMI.RMIServer.HotelierServerInterface;
import model.Hotel;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

/**
 * Questa classe rappresenta il client RMI per interagire con il server di Hotelier tramite invocazioni di metodi remoti.
 * Gestisce la registrazione di un utente, la gestione degli interessi per le città e la gestione delle callback
 * per le modifiche ai ranghi locali degli hotel.
 */
public class HotelierClientRMI {

    private final HotelierClientRmiImp client; // Oggetto client che implementa l'interfaccia per ricevere notifiche
    private HotelierServerInterface stubServer; // Stub del server per invocare metodi remoti sul server
    private HotelierClientInterface stubClient; // Stub del client per essere esposto come oggetto remoto

    /**
     * Costruttore che stabilisce una connessione RMI al server.
     * Viene effettuata la ricerca del server tramite l'indirizzo, il riferimento remoto e la porta specificati.
     * Inoltre, il client viene esportato come oggetto remoto per ricevere callback.
     *
     * @param serverAddress Indirizzo del server RMI
     * @param rmiRemoteReference Riferimento remoto del server
     * @param rmiPort Porta su cui il server RMI è in ascolto
     * @throws Exception Se si verificano errori nella connessione o nell'esportazione dell'oggetto remoto
     */
    public HotelierClientRMI(String serverAddress, String rmiRemoteReference, int rmiPort) throws Exception {
        client = new HotelierClientRmiImp(); // Creazione dell'oggetto client che gestisce la logica delle callback
        Registry registry = LocateRegistry.getRegistry(serverAddress, rmiPort); // Recupera il registro RMI dal server
        stubServer = (HotelierServerInterface) registry.lookup(rmiRemoteReference); // Ottiene lo stub del server
        stubClient = (HotelierClientInterface) UnicastRemoteObject.exportObject(client, 0); // Esporta il client come oggetto remoto per ricevere callback
    }

    /**
     * Richiede la registrazione di un nuovo utente con il server, inviando il nome utente e la password.
     *
     * @param username Nome utente per la registrazione
     * @param password Password associata al nome utente
     * @return Un messaggio di conferma o errore ricevuto dal server
     * @throws RemoteException Se si verifica un errore nella comunicazione con il server
     */
    public String requesteRegisterForNewUser(String username, String password) throws RemoteException {
        return stubServer.registerUser(username, password); // Chiamata remota per registrare l'utente sul server
    }

    /**
     * Registra l'utente per ricevere notifiche di aggiornamento sui ranghi locali degli hotel
     * nelle città di interesse specificate.
     *
     * @param cities Una lista di città di interesse per le quali l'utente desidera ricevere notifiche
     * @throws RemoteException Se si verifica un errore nella comunicazione con il server
     */
    public void registerInterests(List<String> cities) throws RemoteException {
        // Invocazione remota del metodo sul server per registrare il client come callback per le città di interesse
        stubServer.registerCallback(stubClient, cities);
    }

    /**
     * Rimuove la registrazione dell'utente dalle callback per le città di interesse,
     * disabilitando così l'invio di notifiche.
     *
     * @throws RemoteException Se si verifica un errore nella comunicazione con il server
     */
    public void removeInterest() throws RemoteException {
        stubServer.unregisterCallback(stubClient); // Rimuove il client dalle callback nel server
    }

    /**
     * Converte la mappa dei ranghi locali in una stringa formattata, pronta per la stampa.
     * La mappa contiene le città come chiavi e le liste degli hotel come valori.
     *
     * @return Una stringa formattata che rappresenta la mappa dei ranghi locali degli hotel
     */
    public String localRankMapToString() {
        var localRankMap = client.getLocalRankMap(); // Recupera la mappa dei ranghi locali dal client

        StringBuilder sb = new StringBuilder(); // StringBuilder per costruire la stringa formattata
        String separator = "--------------------------------------------------\n"; // Separatore per migliorare la leggibilità

        // Itera sulla mappa e costruisce la stringa formattata
        for (Map.Entry<String, List<Hotel>> entry : localRankMap.entrySet()) {
            sb.append("================" + entry.getKey() + "================").append("\n\n");

            // Aggiunge informazioni sugli hotel di ciascuna città
            for (Hotel hotel : entry.getValue()) {
                sb.append(hotel).append("\n");
                sb.append(separator);
            }
        }

        return sb.toString(); // Restituisce la stringa formattata
    }

    /**
     * Controlla se la mappa dei ranghi locali è vuota. Questo indica se l'utente ha selezionato città di interesse.
     *
     * @return true se la mappa dei ranghi locali è vuota (nessuna città di interesse selezionata),
     *         false se ci sono città selezionate
     */
    public boolean islocalRankMapEmpty() {
        return client.getLocalRankMap().isEmpty(); // Verifica se la mappa dei ranghi locali è vuota
    }

    /**
     * Reset della mappa dei ranghi locali. Rimuove tutti gli hotel e i dati relativi.
     */
    public void resetLocalRankMap() {
        client.getLocalRankMap().clear(); // Pulisce la mappa dei ranghi locali
    }

    /**
     * Chiude le risorse associate alla comunicazione TCP e deregistra il client dalle callback,
     * se necessario. Inoltre, rimuove l'esportazione dello stub del client.
     */
    public void close() {
        try {
            // Se l'utente ha registrato delle città di interesse, lo deregistriamo dalle callback
            if (!islocalRankMapEmpty()) {
                removeInterest(); // Rimuove l'interesse registrato per le città
            }
            // Rimuove l'esportazione dello stub del client, liberando le risorse
            UnicastRemoteObject.unexportObject(client, true);

        } catch (RemoteException e) {
            e.printStackTrace(); // Stampa lo stack trace in caso di errore durante la chiusura
        }
    }
}
