package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public interface HOTELIERClientCallback extends Remote {
    //metodo per ricevere notifiche daò server
    void notifyClient(String message) throws RemoteException;
}
