package server;

import RMI.RMIServer.HotelierServerRMI;
import com.fasterxml.jackson.core.JsonProcessingException;
import model.Hotel;
import model.LocalHotelRanking;
import model.Recensioni;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankingAlgorithm implements Runnable {

    /** DA MODIFICARE
     * La classe HotelierServerRanking gestisce il calcolo e l'aggiornamento periodico dei rank globali e locali degli hotel.
     * Avvia un thread per eseguire ogni rankingInterval secondi le seguenti operazioni:
     * 1. Calcola il rank globale di ogni hotel in base a: numero, qualità e attualità delle sue recensioni.
     * 2. Aggiorna i rank locali degli hotel per ogni città.
     * 3. Serializza e persiste i rank aggiornati degli hotel su disco.
     * 4. In caso di cambiamento della prima posizione del rank locale di una città lo notifica tramite multicast a tutti gli utenti loggati.
     * 5. In caso di cambiamento di rank locale lo notifica tramite callback RMI a tutti gli utenti interessati.
     * L'idea di fondo consiste nel tenere un rank globale per tutti gli hotel e utilizzare quest'ultimo per il calcolo del rank locale
     * degli hotel rispetto alla loro città.
     *
     * Il calcolo del rank globale di un hotel è stato implementato come segue:
     * 1. Ottiene le recensioni dell'hotel.
     * 2. Calcola la media dei giorni trascorsi dalla pubblicazione delle recensioni.
     * 3. Calcola il nuovo rank globale utilizzando la seguente formula:
     *    • (rate * numero recensioni) + (1 / (1 + media giorni trascorsi))
     *    La formula dà più peso alla qualità e quantità delle recensioni, e tiene conto anche dell'attualità delle recensioni.
     */

    // Intervallo in secondi tra i calcoli dei rank
    private final int rankingInterval;
    // server RMI
    private final HotelierServerRMI serverRmi;
    // multicast sender
    private final HotelierServerMulticast multicastSender;
    // registro hotel
    private final HotelierServerHotelManager hotelRegister;
    // registro recensioni
    private final HotelierServerReviewManager reviewRegister;
    // lista di local rank
    private final List<LocalHotelRanking> localRanks;

    public RankingAlgorithm(int rankingInterval, HotelierServerRMI serverRmi, HotelierServerMulticast multicastSender) {
        // set ranking interval
        this.rankingInterval = rankingInterval;
        this.serverRmi = serverRmi;
        this.multicastSender = multicastSender;
        hotelRegister = HotelierServerHotelManager.getInstance();
        reviewRegister = HotelierServerReviewManager.getInstance();
        localRanks = new ArrayList<>();

        // start the thread
        Thread thread = new Thread(this);
        thread.start();
    }

    // Initialize local rank list used to track changes
    private void initializeLocalRanks() {
        var cities = hotelRegister.getCities();
        for (String city : cities) {
            var hotels = hotelRegister.getHotelsByCity(city);
            Collections.sort(hotels, Comparator.comparingInt(Hotel::getLocalRank));
            var localRank = new LocalHotelRanking(city);

            for (Hotel hotel : hotels) {
                localRank.add(new Hotel(hotel));
            }

            localRanks.add(localRank);
        }
    }

    // Updates local ranks for all hotels in the register
    private void udpateHotelsLocalRank() {
        var cities = hotelRegister.getCities();
        for (String city : cities) {
            var hotels = hotelRegister.getHotelsByCity(city);
            Collections.sort(hotels, Comparator.comparingDouble(Hotel::getRank).reversed());

            for (int i = 0; i < hotels.size(); i++) {
                var hotel = hotels.get(i);
                hotel.setLocalRanking(i + 1);
            }
        }
    }

    @Override
    public void run() {
        initializeLocalRanks();

        while (!Thread.interrupted()) {
            var hotels = hotelRegister.getHotels();
            for (Hotel hotel : hotels) {
                if (hotel.getReviewCount() != 0) {
                    var rank = calculateRank(hotel);
                    hotel.setRankLevel(rank);
                }
            }

            udpateHotelsLocalRank();
            hotelRegister.serialize();

            for (LocalHotelRanking localRank : localRanks) {
                var localRankHotels = localRank.getHotels();
                var localRankCity = localRank.getCity();
                var cityHotels = hotelRegister.getHotelsByCity(localRankCity);
                Collections.sort(localRankHotels, Comparator.comparingInt(Hotel::getLocalRank));
                Collections.sort(cityHotels, Comparator.comparingInt(Hotel::getLocalRank));
                var localRankFirstHotel = localRankHotels.get(0);
                var cityHotelsFirstHotel = cityHotels.get(0);

                if (localRankFirstHotel.getId() != cityHotelsFirstHotel.getId()) {
                    multicastSender.notifyFirstPosition(cityHotelsFirstHotel);
                }

                if (isLocalRankChanged(localRankHotels, cityHotels)) {
                    List<Hotel> cityHotelsCopy = new ArrayList<>();
                    for (Hotel hotel : cityHotels) {
                        cityHotelsCopy.add(new Hotel(hotel));
                    }
                    localRank.setHotels(cityHotelsCopy);
                    try {
                        serverRmi.notifyLocalRank(localRank);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            sleep(rankingInterval);
        }
    }

    // Check if local rank has changed (lists have different size/order)
    private boolean isLocalRankChanged(List<Hotel> localRankHotels, List<Hotel> cityHotels) {
        if (localRankHotels.size() != cityHotels.size()) {
            return true;
        }

        for (int i = 0; i < localRankHotels.size(); i++) {
            var localHotel = localRankHotels.get(i);
            var cityHotel = cityHotels.get(i);
            if (localHotel.getId() != cityHotel.getId()) {
                return true;
            }
        }
        return false;
    }

    // Calculates and returns the new global rank of the hotel
    private double calculateRank(Hotel hotel) {

        // ottengo la lista di recensioni dell'hotel
        var reviews = reviewRegister.getHotelReviews(hotel);

        // variabile per calcolo della media dei giorni trascorsi
        int totalDays = 0;

        // calcola i giorni trascorsi dalla pubblicazione di ogni recensione
        for (Recensioni review : reviews) {
            var timestamp = LocalDateTime.parse(review.getTimestamp());
            totalDays += ChronoUnit.DAYS.between(timestamp, LocalDateTime.now());
        }

        // calcola la media dei giorni trascorsi
        int reviewCount = hotel.getReviewCount();
        double avgDays = reviewCount > 0 ? (double) totalDays / reviewCount : 0;

        // calcola il peso dei giorni
        double pesoGiorni = Math.max(0.5, 1 - avgDays / 100);

        // ottieni la media delle valutazioni dell'hotel
        double rate = hotel.getRate();

        // calcola il rank basato su rate e reviewCount, influenzato dai giorni
        double rank = (rate * pesoGiorni) + (reviewCount / 2.0);

        // assicura che il rank sia compreso tra 1 e 5
        return Math.min(5, Math.max(1, rank));
    }

    private void sleep(int rankingInterval) {
        try {
            Thread.sleep(rankingInterval * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
