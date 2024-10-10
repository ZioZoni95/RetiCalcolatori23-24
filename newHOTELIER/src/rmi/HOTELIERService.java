package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HOTELIERService extends Remote {
    //Metodo per registrare un ututente e inviare una notifica al clietn
    String registerUser(String username, String password, String email,
                        HOTELIERClientCallback clientCallback) throws RemoteException;
    String logOut(String username, String password, String email,
                        HOTELIERClientCallback clientCallback) throws RemoteException;
}
