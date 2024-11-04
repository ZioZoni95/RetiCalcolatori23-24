package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HotelierService extends Remote {
    // Metodo per la registrazione
    String register(String username, String password) throws RemoteException;

    // Metodo per il login
    String login(String username, String password) throws RemoteException;

    // Metodo per il logout
    String logout(String username) throws RemoteException;
}