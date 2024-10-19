package server;

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
        HotelierServer_NIO_TCP tcpServer = new HotelierServer_NIO_TCP("localhost:8081 Cioa TCP",8081);
        tcpServer.run(); //avvia il server TCP
    }
}
