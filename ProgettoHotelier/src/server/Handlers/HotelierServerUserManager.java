package server.Handlers;

import model.Utente;
import org.apache.commons.lang3.StringUtils;
import utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static server.config.ServerJsonSettings.USERS_PATH_JSON;

/**
 * La classe HotelierServerUserManager gestisce gli utenti all'interno del sistema Hotelier.
 * Permette di autenticare gli utenti, registrare nuovi utenti, recuperare un utente tramite nome e serializzare/deserializzare
 * la lista degli utenti per la persistenza su disco. Utilizza una lista sincronizzata per garantire che le operazioni
 * siano sicure in un ambiente multithreading.
 */
public class HotelierServerUserManager {

    // Istanza unica della classe (Singleton)
    public static HotelierServerUserManager instance = null;

    /**
     * Metodo per ottenere l'istanza unica di HotelierServerUserManager (Singleton).
     * Se l'istanza non esiste ancora, viene creata.
     *
     * @return L'istanza unica di HotelierServerUserManager.
     */
    public static HotelierServerUserManager getInstance(){
        if(instance == null){
            instance = new HotelierServerUserManager();
        }
        return instance;
    }

    // Lista contenente tutti gli utenti registrati nel sistema
    private List<Utente> users;

    /**
     * Costruttore privato della classe, inizializza la lista degli utenti.
     * Viene chiamato solo all'interno della classe per garantire il pattern Singleton.
     */
    private HotelierServerUserManager(){
        users = new ArrayList<>();
    }

    /**
     * Metodo per autenticare un utente, verificando che username e password corrispondano
     * a quelli di un utente esistente.
     *
     * @param username Il nome utente da autenticare.
     * @param password La password da verificare.
     * @return L'oggetto Utente se l'autenticazione è riuscita, null altrimenti.
     */
    public Utente Authentication(String username, String password){
        synchronized (users){
            for(Utente user: users){
                // Confronta username e password (case insensitive)
                if(StringUtils.equalsIgnoreCase(username, user.getUsername()) &&
                        StringUtils.equalsIgnoreCase(password, user.getPassword())){
                    return user;
                }
            }
        }
        // Restituisce null se l'autenticazione non ha avuto successo
        return null;
    }

    /**
     * Metodo per registrare un nuovo utente nel sistema.
     * Verifica che lo username e la password siano validi e non già presenti nel sistema.
     *
     * @param username Il nome utente da registrare.
     * @param password La password da associare all'utente.
     * @return Un messaggio che indica l'esito della registrazione (successo o errore).
     */
    public String register(String username, String password){
        // Verifica che lo username e la password non siano vuoti
        if(username.isEmpty() || password.isEmpty()){
            return "Errore: Username e Password vuoti";
        }

        // Verifica che non ci siano spazi vuoti nei campi username e password
        if(StringUtils.containsWhitespace(username) || password.isEmpty()){
            return "Errore Caratteri: Spazi vuoti non permessi in Username e Password";
        }

        synchronized (users){
            // Verifica se l'utente esiste già nel sistema
            for(Utente user : users){
                if (StringUtils.equalsIgnoreCase(username, user.getUsername())){
                    return "Errore Registrazione: Utente già presente";
                }
            }

            // Crea un nuovo oggetto Utente e lo aggiunge alla lista
            Utente user = new Utente(username, password);
            users.add(user);
        }

        // Serializza la lista degli utenti dopo aver aggiunto un nuovo utente
        serialize();

        return "Nuovo Utente : " + username + " è stato registrato con successo";
    }

    /**
     * Metodo per recuperare un utente tramite il suo nome utente.
     * Se un utente con quel nome utente esiste, viene restituito l'oggetto Utente.
     *
     * @param username Il nome utente dell'utente da recuperare.
     * @return L'oggetto Utente corrispondente al nome utente, oppure null se non trovato.
     */
    public Utente getUserByName(String username){
        synchronized (users){
            // Cerca l'utente nella lista confrontando il nome utente
            for(Utente user : users){
                if(StringUtils.equalsIgnoreCase(username, user.getUsername())){
                    return user;
                }
            }
        }
        // Restituisce null se l'utente non viene trovato
        return null;
    }

    /**
     * Metodo per serializzare la lista degli utenti in formato JSON e salvarla su disco.
     * In caso di errore durante la scrittura, viene stampato lo stack trace.
     */
    public void serialize(){
        try{
            synchronized (users){
                // Converte la lista degli utenti in una stringa JSON
                String jsonUser = JsonUtils.serialize(users);
                // Scrive la stringa JSON su disco nel file di persistenza
                JsonUtils.writeFile(jsonUser, new File(USERS_PATH_JSON));
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Metodo per deserializzare la lista degli utenti da un file JSON e aggiungerla alla lista degli utenti.
     * Se si verifica un errore durante la lettura o deserializzazione, viene stampato lo stack trace.
     */
    public void deserialize(){
        try{
            synchronized (users){
                // Legge il file JSON che contiene gli utenti
                var userFile = new File(USERS_PATH_JSON);
                var userJson = JsonUtils.readFile(userFile);
                // Converte il JSON in una lista di oggetti Utente
                var deserializedUsers = Arrays.asList(JsonUtils.deserialize(userJson, Utente[].class));
                // Aggiunge gli utenti deserializzati alla lista principale
                users.addAll(deserializedUsers);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
