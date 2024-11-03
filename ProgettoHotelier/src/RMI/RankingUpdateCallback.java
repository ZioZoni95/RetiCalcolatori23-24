package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RankingUpdateCallback extends Remote {
    void RankUpdate (String hotelName, String City) throws RemoteException;
}
