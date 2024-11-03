package RMI;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HotelierService extends Remote {
    boolean register(String username, String password) throws RemoteException;
    boolean login(String username, String password) throws RemoteException;
    void registerForRankingUpdates(String city, RankingUpdateCallback callback) throws RemoteException;
    void unregisterForRankingUpdates(String city, RankingUpdateCallback callback) throws RemoteException;
}
