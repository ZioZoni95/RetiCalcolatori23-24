package server;
import rmi.HOTELIERService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HOTELIERServerMain {
    public static void main(String[] args){
        try {
            //avvio il server RMI
            startRMIServer();

            //avvio il server TCP
            startTCPServer();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void startRMIServer() throws RemoteException {
        //crea un'istanza del servizio
        HOTELIERServer rmiServer = new HOTELIERServer();

        //Crea il registry RMI sulla porta
        Registry registry = LocateRegistry.createRegistry(1099);

        //registra il servizio
        registry.rebind("HOTELIERService", rmiServer);

        System.out.println("Server RMI avviato e registrato sul Registry");
    }

    private static void startTCPServer() {
        HotelierServerTCP tcpServer = new HotelierServerTCP();
        tcpServer.startServer(); //avvia il server TCP
    }
}
