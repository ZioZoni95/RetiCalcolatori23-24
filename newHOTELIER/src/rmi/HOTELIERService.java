package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HOTELIERService extends Remote {
    //Metodo per registrare un ututente e inviare una notifica al clietn
    String registerUser(String username, String password,
                        HOTELIERClientCallback clientCallback) throws RemoteException;
    String logInUser(String username, String password, String email,
                        HOTELIERClientCallback clientCallback) throws RemoteException;

    /**
     * metodo per effettuare il logout di un utente
     * @throws RemoteException
     */
    void logOUTUser(String username) throws RemoteException ;
    /**
     * Mancano i metodi per gli Hotel
     */

}
