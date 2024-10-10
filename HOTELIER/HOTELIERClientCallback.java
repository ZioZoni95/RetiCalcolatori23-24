package HOTELIER;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HOTELIERClientCallback extends Remote {
    //metodo per ricevere notifiche daò server
    void notifyClient(String message) throws RemoteException;
}
