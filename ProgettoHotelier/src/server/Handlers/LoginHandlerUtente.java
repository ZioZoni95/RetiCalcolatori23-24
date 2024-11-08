package server.Handlers;

import model.Utente;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * La classe LoginHandlerUtente gestisce la gestione degli utenti loggati nel sistema.
 * Essa tiene traccia degli utenti che hanno effettuato l'accesso e fornisce i metodi per aggiungere, verificare
 * e rimuovere gli utenti dalla lista degli utenti loggati. La classe è implementata utilizzando il pattern Singleton
 * per garantire che esista una sola istanza della gestione degli utenti loggati.
 */
public class LoginHandlerUtente {

    // Variabile statica che tiene traccia dell'istanza unica della classe (Singleton)
    private static LoginHandlerUtente istanza = null;

    /**
     * Costruttore privato della classe per evitare istanze multiple (Singleton).
     * Inizializza la lista degli utenti loggati come una nuova ArrayList.
     */
    private LoginHandlerUtente() {
        utentiLoggati = new ArrayList<>();
    }

    /**
     * Metodo che restituisce l'istanza unica di LoginHandlerUtente.
     * Se l'istanza non è ancora stata creata, viene generata una nuova istanza.
     *
     * @return L'istanza unica di LoginHandlerUtente.
     */
    public static LoginHandlerUtente getInstance() {
        if (istanza == null) {
            istanza = new LoginHandlerUtente();
        }
        return istanza;
    }

    // Lista che tiene traccia degli utenti attualmente loggati
    private List<Utente> utentiLoggati;

    /**
     * Aggiunge un utente alla lista degli utenti loggati.
     * Questo metodo è sincronizzato per evitare condizioni di race in ambienti multithreading.
     *
     * @param user L'utente da aggiungere alla lista degli utenti loggati.
     */
    public synchronized void addLoggedUser(Utente user) {
        utentiLoggati.add(user);
    }

    /**
     * Verifica se un utente è già loggato.
     * Il controllo viene effettuato confrontando lo username dell'utente passato con quelli presenti nella lista.
     *
     * @param user L'utente da verificare.
     * @return true se l'utente è loggato, false altrimenti.
     */
    public synchronized boolean userIsLogged(Utente user) {

        for (Utente utentiLoggati : utentiLoggati) {
            // Confronta lo username dell'utente passato con quelli degli utenti loggati
            if (StringUtils.equalsIgnoreCase(utentiLoggati.getUsername(), user.getUsername())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rimuove un utente dalla lista degli utenti loggati.
     * Questo metodo è sincronizzato per evitare accessi concorrenti sulla lista.
     *
     * @param user L'utente da rimuovere dalla lista degli utenti loggati.
     */
    public synchronized void removeUser(Utente user) {
        utentiLoggati.remove(user);
    }
}