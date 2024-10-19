package server.rmi;

import rmi.HOTELIERClientCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HOTELIERServerRMI extends Remote {
    //Metodo per registrare un ututente e inviare una notifica al clietn
    public String registerUser(String username, String password) throws RemoteException;
   /*OLD String logInUser(String username, String password,
                        HOTELIERClientCallback clientCallback) throws RemoteException;

    /**
     * metodo per effettuare il logout di un utente
     * @throws RemoteException

    void logOUTUser(String username) throws RemoteException ;
    /**
     * Mancano i metodi per gli Hotel
     */
}
