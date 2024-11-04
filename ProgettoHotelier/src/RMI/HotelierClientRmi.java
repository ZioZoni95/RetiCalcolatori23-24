package RMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

public class HotelierClientRmi {
    private HotelierService server;

    public HotelierClientRmi(String host, int port) throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        server = (HotelierService) registry.lookup("HotelierServer");
    }

    /**
     * Registra un nuovo utente con il server RMI.
     */
    public String register(String username, String password) throws RemoteException {
        return server.register(username, password);
    }

    /**
     * Effettua il login di un utente con il server RMI.
     */
    public String login(String username, String password) throws RemoteException {
        return server.login(username, password);
    }

    /**
     * Effettua il logout di un utente con il server RMI.
     */
    public String logout(String username) throws RemoteException {
        return server.logout(username);
    }
}
