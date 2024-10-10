package rmi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;
import java.io.IOException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

/**
 * Test mode 10/10/2024
 */

public class HOTELIERServer extends UnicastRemoteObject implements HOTELIERService {
    private List<Utente> users;

    //costruttore del server
    protected HOTELIERServer() throws RemoteException{
        super();

    }

    /**
     * Metodo per registrare un utente e notificare il client mediante callback
     */
    @Override
    public String registerUser(String username, String password, String email,
                               HOTELIERClientCallback clientCallback) throws RemoteException{
        //verifica se esiste user
        for(Utente user : users){
            if(user.getUsername().equals(username)){
                clientCallback.notifyClient("Errore: Nome già in uso.");
                return "ERROR: Nome utente in uso";
            }
            if(user.geteMail().equals(email)){
                clientCallback.notifyClient("Errore: Email in uso.");
                return "ERROR: Email in uso";
            }
        }

        //Crea un nuovo user e lo aggiunge alla lista degli utenti
        Utente newUser = new Utente(username,password,email);
        users.add(newUser);

        clientCallback.notifyClient("Registrazione avvenuta con Successo per l'utente " + username);
        return "SUCCESS";
    }

    /**
     *
     */

}
