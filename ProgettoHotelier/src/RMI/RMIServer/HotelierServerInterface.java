package RMI.RMIServer;

import RMI.RMIClient.HotelierClientInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface HotelierServerInterface extends Remote {
    // Metodo per la registrazione
    public String registerUser(String username, String password) throws RemoteException;

    // Metodo per il login
    public void registerCallback(HotelierClientInterface clientCallback, List<String> city) throws RemoteException;

    public void unregisterCallback(HotelierClientInterface callbackClient) throws RemoteException;
}
