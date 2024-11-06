package server;
import Handlers.LoginHandlerUtente;
import RMI.HotelierServerServiceInterfaceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HotelierMainServer {
    public static void main(String[] args) {
        try {
            // Avvia il server TCP/NIO per la ricerca hotel
            int tcpPort = 9999;
            HotelierServer_NIO_TCP hotelServer = new HotelierServer_NIO_TCP(tcpPort);
            new Thread(hotelServer).start();

            // Configura e avvia il server RMI per la gestione utenti
            LoginHandlerUtente userHandler = new LoginHandlerUtente();
            HotelierServerServiceInterfaceImpl userServer = new HotelierServerServiceInterfaceImpl(userHandler);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("HotelierServer", userServer);

            System.out.println("Server RMI per utenti registrato su porta 1099.");
            System.out.println("Server TCP/NIO per hotel avviato su porta " + tcpPort);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}