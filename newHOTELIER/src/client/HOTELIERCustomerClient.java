package client;
import rmi.HOTELIERClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class HOTELIERCustomerClient extends UnicastRemoteObject implements HOTELIERClientCallback {
    /**
     * Costruttore del client
     */
    protected HOTELIERCustomerClient() throws RemoteException{
        super();
    }

    /**
     * Implementazione del metodo per Ricevere notifiche
     */
    @Override
    public void notifyClient(String message) throws RemoteException {
        System.out.println("Notifica del server " + message);
    }
    /**
     * TEST MODE 10/10/2024
     */
}
