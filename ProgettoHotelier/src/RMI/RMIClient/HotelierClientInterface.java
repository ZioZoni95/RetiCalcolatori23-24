package RMI.RMIClient;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HotelierClientInterface extends Remote {
    public void notifyInterest (String serializedLocalRank) throws RemoteException;
}
