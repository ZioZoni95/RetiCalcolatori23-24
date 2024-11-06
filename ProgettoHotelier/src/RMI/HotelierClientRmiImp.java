package RMI;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Hotel;
import model.LocalHotelRanking;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotelierClientRmiImp implements HotelierClientInterface {
    /**
     * La classe HotelierClientRmiImpl fornisce le funzionalità necessarie per gestire le callback da HotelierServerRmi,
     * mediante la mappa dei rank locali (localRankMap), avente:
     * • chiave: città;
     * • valore: la lista degli hotel ordinati per rank locale.
     * A seguito della ricezione di una callback, deserializza il rank locale ricevuto e aggiorna la entry corrispondente
     * della mappa con la nuova lista di hotel ordinati per rank locale.
     * La classe utilizza la sincronizzazione per garantire la coerenza dei dati quando accede/modifica la mappa dei rank locali.
     */

    // mappa dei rank locali con chiave: città e valore: lista hotel ordinati per rank locale
    private final Map<String, List<Hotel>> localRankMap;

    private final ObjectMapper objectMapper;

    public HotelierClientRmiImp() {
        localRankMap = new HashMap<>();
        objectMapper = new ObjectMapper(); // Utilizziamo ObjectMapper di Jackson
    }

    // metodo invocato da remoto da HotelierServerRmi per notificare cambiamento di un local rank
    @Override
    public synchronized void notifyInterest(String serializedLocalRank) throws RemoteException {

        try {
            // Deserializzazione del local rank
           LocalHotelRanking localRank = objectMapper.readValue(serializedLocalRank, LocalHotelRanking.class);
            // Ottengo città del local rank
            String city = localRank.getCity();
            // Ottengo lista hotel ordinate del local rank
            List<Hotel> hotels = localRank.getHotels();

            // Eseguo update della mappa dei local rank
            synchronized (localRankMap) {
                localRankMap.put(city, hotels);
            }
        } catch (Exception e) {
            throw new RemoteException("Errore durante la deserializzazione del Rank locale", e);
        }
    }

    // Restituisco la mappa dei local rank
    public Map<String, List<Hotel>> getLocalRankMap() {
        return localRankMap;
    }
}

    /*
    private HotelierServerServiceInterface server;


    public HotelierClientRmi(String host, int port) throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        server = (HotelierServerServiceInterface) registry.lookup("HotelierServer");
    }

    /**
     * Registra un nuovo utente con il server RMI.
     *//*
    public String register(String username, String password) throws RemoteException {
        return server.register(username, password);
    }

    /**
     * Effettua il login di un utente con il server RMI.
     *//*
    public String login(String username, String password) throws RemoteException {
        return server.login(username, password);
    }

    /**
     * Effettua il logout di un utente con il server RMI.
     *//*
    public String logout(String username) throws RemoteException {
        return server.logout(username);
    }*/