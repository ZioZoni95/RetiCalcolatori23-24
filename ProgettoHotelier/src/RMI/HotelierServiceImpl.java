package RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HotelierServiceImpl extends UnicastRemoteObject implements HotelierService {
    private final Map<String, String> users = new ConcurrentHashMap<>();
    private final Map<String, Set<RankingUpdateCallback>> rankingSubscribers = new ConcurrentHashMap<>();

    protected HotelierServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized boolean register(String username, String password) throws RemoteException {
        if (users.containsKey(username)) return false;
        users.put(username, password);
        return true;
    }

    @Override
    public boolean login(String username, String password) throws RemoteException {
        return password.equals(users.get(username));
    }

    @Override
    public void registerForRankingUpdates(String city, RankingUpdateCallback callback) throws RemoteException {
        rankingSubscribers.computeIfAbsent(city, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(callback);
    }

    @Override
    public void unregisterForRankingUpdates(String city, RankingUpdateCallback callback) throws RemoteException {
        Set<RankingUpdateCallback> subscribers = rankingSubscribers.get(city);
        if (subscribers != null) {
            subscribers.remove(callback);
            if (subscribers.isEmpty()) {
                rankingSubscribers.remove(city);
            }
        }
    }

    public void notifyRankingChange(String hotelName, String city) {
        Set<RankingUpdateCallback> subscribers = rankingSubscribers.get(city);
        if (subscribers != null) {
            for (RankingUpdateCallback callback : subscribers) {
                try {
                    callback.RankUpdate(hotelName, city);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
