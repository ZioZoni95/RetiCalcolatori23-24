package hotelier.rmi;

import hotelier.util.Result;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/** Servizi esposti dal server via RMI. */
public interface ServerService extends Remote {

    /** Registra un nuovo utente. */
    Result register(String username, String password) throws RemoteException;

    /**
     * Registra il client per ricevere le variazioni delle classifiche locali delle città indicate.
     * La classifica corrente di ciascuna città viene inviata subito.
     */
    void registerCallback(ClientCallback callback, List<String> cities) throws RemoteException;

    void unregisterCallback(ClientCallback callback) throws RemoteException;
}
