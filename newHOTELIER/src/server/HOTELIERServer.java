package server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Utente;
import rmi.HOTELIERClientCallback;
import rmi.HOTELIERService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test mode 10/10/2024 ore 21:38 Implementato login logout e registrazione
 */

public class HOTELIERServer extends UnicastRemoteObject implements HOTELIERService {
    /**
     * Lista degli utenti registrati
     */
    private List<Utente> users = null;

    /**
     * Attributi per gli hotel e Map da aggiungere
     */

    /**
     * Mappa per gestire gli utenti loggati
     */
    private Map<String, HOTELIERClientCallback> loggedClients;

    //costruttore del server
    protected HOTELIERServer() throws RemoteException {
        super();
        loggedClients = new HashMap<>(); // inizializza la mappa degli utenti loggati
    }

    /**
     * Metodo per registrare un utente e notificare il client mediante callback
     */
    @Override
    public String registerUser(String username, String password,
                               HOTELIERClientCallback clientCallback) throws RemoteException {
        //verifica se esiste user
        for (Utente user : users) {
            if (user.getUsername().equals(username)) {
                clientCallback.notifyClient("Errore: Nome già in uso.");
                return "ERROR: Nome utente in uso";
            }
            if (password == null || password.isEmpty()) {
                clientCallback.notifyClient("Errore: La password non può essere vuota");
                return "ERROR: La password non può essere vuota";
            }

            /*if (user.geteMail().equals(email)) {
                clientCallback.notifyClient("Errore: Email in uso.");
                return "ERROR: Email in uso";
            }*/
        }

        //Crea un nuovo user e lo aggiunge alla lista degli utenti
        Utente newUser = new Utente(username, password);
        users.add(newUser);
        saveUsersToFile(users, "resources/utenti.json");
        clientCallback.notifyClient("Registrazione avvenuta con Successo per l'utente " + username);
        return "SUCCESS";
    }

    @Override
    public String logInUser(String username, String password, HOTELIERClientCallback clientCallback) throws RemoteException {
        for (Utente user : users) { //da controllare
            if (user.getUsername().equals(username)) {
                if (user.checkPassword(password)) {
                    loggedClients.put(username, clientCallback);
                    clientCallback.notifyClient("Login avvenuto con successo per l'utente " + username);
                    return "SUCCESS";
                } else {
                    clientCallback.notifyClient("Errore: Password errata.");
                    return "Error: Password errata";
                }
            }
        }
        clientCallback.notifyClient("Errore, Utente non trovato ");
        return "Error: Utente non trovato:";
    }


    @Override
    public void logOUTUser(String username) throws RemoteException {
        if (loggedClients.remove(username) != null) { //rimuovi il client dalla mappa dei loggati
            System.out.println("L'utente " + username + "ha effettuato il logout");
        } else {
            System.out.println("L'utente " + username + "non era loggato");
        }
    }

    //persistenza file user json
    private List<Utente> loadUsersFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath);

        if (!file.exists() || file.length() == 0) {
            System.out.println("Nessun dato trovato nel file degli utenti. Creazione di un nuovo file");
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(file, new TypeReference<List<Utente>>() {
            });
        } catch (IOException e) {
            System.out.println("Errore nel caricamento dei dati degli utenyi " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveUsersToFile(List<Utente> users, String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(filePath), users);
            System.out.println("Dati degli utenti salvati correttamente.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyClients(String message) throws RemoteException {
        for (HOTELIERClientCallback clientCallback : loggedClients.values()) {
            try {
                clientCallback.notifyClient(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}


