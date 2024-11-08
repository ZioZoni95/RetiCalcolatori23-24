package server;

import RMI.RMIServer.HotelierServerRMI;
import com.fasterxml.jackson.core.JsonProcessingException;
import model.Hotel;
import model.LocalHotelRanking;
import model.Recensioni;
import server.Handlers.HotelierServerReviewManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankingAlgorithm implements Runnable {

    /**
     * La classe RankingAlgorithm gestisce il calcolo e l'aggiornamento periodico dei rank globali e locali degli hotel.
     * Avvia un thread che esegue, ogni intervallo di tempo definito (rankingInterval), i seguenti passaggi:
     * 1. Calcola il rank globale di ogni hotel sulla base di: numero, qualità e attualità delle sue recensioni.
     * 2. Aggiorna i rank locali degli hotel per ogni città.
     * 3. Serializza e persiste i rank aggiornati degli hotel su disco.
     * 4. In caso di cambiamento della prima posizione del rank locale di una città, lo notifica tramite multicast a tutti gli utenti loggati.
     * 5. In caso di cambiamento del rank locale, lo notifica tramite callback RMI a tutti gli utenti interessati.
     *
     * Il calcolo del rank globale di un hotel è eseguito come segue:
     * 1. Ottiene tutte le recensioni dell'hotel.
     * 2. Calcola la media dei giorni trascorsi dalla pubblicazione di ogni recensione.
     * 3. Calcola il nuovo rank globale con la formula:
     *    • (rate * numero recensioni) + (1 / (1 + media giorni trascorsi))
     *    La formula dà più peso alla qualità e quantità delle recensioni, e tiene conto dell'attualità delle recensioni.
     */

    // Intervallo in secondi tra ciascun calcolo del rank
    private final int rankingInterval;
    // Riferimento al server RMI per inviare notifiche agli utenti loggati
    private final HotelierServerRMI serverRmi;
    // Riferimento all'oggetto per inviare notifiche tramite multicast
    private final HotelierServerMulticast multicastSender;
    // Riferimento al gestore degli hotel per accedere alla lista di hotel
    private final HotelierServerHotelManager hotelRegister;
    // Riferimento al gestore delle recensioni per accedere ai dati delle recensioni degli hotel
    private final HotelierServerReviewManager reviewRegister;
    // Lista che tiene traccia dei rank locali per ciascuna città
    private final List<LocalHotelRanking> localRanks;

    /**
     * Costruttore della classe. Inizializza l'intervallo per il calcolo del ranking, i riferimenti ai componenti
     * del sistema (server RMI, multicast sender, gestori di hotel e recensioni) e avvia un nuovo thread per eseguire
     * periodicamente l'algoritmo di ranking.
     *
     * @param rankingInterval Intervallo in secondi per calcolare e aggiornare i rank.
     * @param serverRmi Riferimento al server RMI per le notifiche.
     * @param multicastSender Riferimento al multicast sender per inviare notifiche tramite multicast.
     */
    public RankingAlgorithm(int rankingInterval, HotelierServerRMI serverRmi, HotelierServerMulticast multicastSender) {
        // Imposta l'intervallo per i calcoli dei rank
        this.rankingInterval = rankingInterval;
        this.serverRmi = serverRmi;
        this.multicastSender = multicastSender;
        // Ottieni i riferimenti per il gestore degli hotel e delle recensioni
        hotelRegister = HotelierServerHotelManager.getInstance();
        reviewRegister = HotelierServerReviewManager.getInstance();
        localRanks = new ArrayList<>();

        // Avvia il thread che eseguirà periodicamente il calcolo del ranking
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Inizializza la lista dei rank locali, che tiene traccia dei rank di tutti gli hotel in ciascuna città.
     * Per ogni città, la lista degli hotel viene ordinata in base al rank locale e il ranking locale per quella città
     * viene registrato nella lista `localRanks`.
     */
    private void initializeLocalRanks() {
        // Ottieni l'elenco delle città degli hotel
        var cities = hotelRegister.getCities();
        // Per ogni città, ottieni gli hotel e calcola il loro ranking
        for (String city : cities) {
            var hotels = hotelRegister.getHotelsByCity(city);
            // Ordina gli hotel per rank locale
            Collections.sort(hotels, Comparator.comparingInt(Hotel::getLocalRank));
            var localRank = new LocalHotelRanking(city);

            // Aggiungi ogni hotel al ranking locale per la città
            for (Hotel hotel : hotels) {
                localRank.add(new Hotel(hotel));
            }

            // Aggiungi il ranking locale alla lista dei rank locali
            localRanks.add(localRank);
        }
    }

    /**
     * Aggiorna i rank locali di tutti gli hotel nel registro, ordinandoli in base al rank globale.
     */
    private void udpateHotelsLocalRank() {
        // Ottieni l'elenco delle città
        var cities = hotelRegister.getCities();
        // Per ogni città, aggiorna il ranking locale degli hotel
        for (String city : cities) {
            var hotels = hotelRegister.getHotelsByCity(city);
            // Ordina gli hotel in base al rank globale (decrescente)
            Collections.sort(hotels, Comparator.comparingDouble(Hotel::getRank).reversed());

            // Assegna il rank locale a ciascun hotel
            for (int i = 0; i < hotels.size(); i++) {
                var hotel = hotels.get(i);
                hotel.setLocalRanking(i + 1);  // Il rank locale è 1-based
            }
        }
    }

    /**
     * Esegue il calcolo periodico dei rank globali e locali, aggiornando i ranking di tutti gli hotel e
     * inviando notifiche in caso di cambiamenti significativi.
     */
    @Override
    public void run() {
        // Inizializza i rank locali
        initializeLocalRanks();

        // Ciclo principale che continua finché il thread non viene interrotto
        while (!Thread.interrupted()) {
            // Ottieni l'elenco di tutti gli hotel
            var hotels = hotelRegister.getHotels();
            // Per ogni hotel, calcola e aggiorna il rank globale
            for (Hotel hotel : hotels) {
                if (hotel.getReviewCount() != 0) {
                    var rank = calculateRank(hotel);  // Calcola il nuovo rank globale
                    hotel.setRankLevel(rank);  // Aggiorna il rank globale dell'hotel
                }
            }

            // Aggiorna i rank locali per tutte le città
            udpateHotelsLocalRank();
            // Serializza e persiste i dati aggiornati sul disco
            hotelRegister.serialize();

            // Controlla se ci sono cambiamenti nei rank locali
            for (LocalHotelRanking localRank : localRanks) {
                var localRankHotels = localRank.getHotels();
                var localRankCity = localRank.getCity();
                var cityHotels = hotelRegister.getHotelsByCity(localRankCity);
                // Ordina i ranking locali e quelli per città
                Collections.sort(localRankHotels, Comparator.comparingInt(Hotel::getLocalRank));
                Collections.sort(cityHotels, Comparator.comparingInt(Hotel::getLocalRank));
                var localRankFirstHotel = localRankHotels.get(0);
                var cityHotelsFirstHotel = cityHotels.get(0);

                // Se la prima posizione del ranking locale è cambiata, invia una notifica tramite multicast
                if (localRankFirstHotel.getId() != cityHotelsFirstHotel.getId()) {
                    multicastSender.notifyFirstPosition(cityHotelsFirstHotel);
                }

                // Se il ranking locale è cambiato, invia una notifica tramite RMI
                if (isLocalRankChanged(localRankHotels, cityHotels)) {
                    List<Hotel> cityHotelsCopy = new ArrayList<>();
                    for (Hotel hotel : cityHotels) {
                        cityHotelsCopy.add(new Hotel(hotel));  // Copia i dati dell'hotel per la notifica
                    }
                    localRank.setHotels(cityHotelsCopy);  // Aggiorna la lista di hotel del ranking locale
                    try {
                        serverRmi.notifyLocalRank(localRank);  // Notifica tramite RMI
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);  // Gestisce eventuali errori di serializzazione
                    }
                }
            }
            // Pausa prima di eseguire nuovamente il calcolo del ranking
            sleep(rankingInterval);
        }
    }

    /**
     * Verifica se il ranking locale di una città è cambiato confrontando l'ordine degli hotel tra
     * il ranking locale precedente e quello corrente.
     *
     * @param localRankHotels Lista di hotel nel ranking locale precedente.
     * @param cityHotels Lista di hotel nel ranking corrente della città.
     * @return true se il ranking locale è cambiato, altrimenti false.
     */
    private boolean isLocalRankChanged(List<Hotel> localRankHotels, List<Hotel> cityHotels) {
        if (localRankHotels.size() != cityHotels.size()) {
            return true;  // Se il numero di hotel differisce, i rank locali sono cambiati
        }

        // Confronta gli hotel uno ad uno
        for (int i = 0; i < localRankHotels.size(); i++) {
            var localHotel = localRankHotels.get(i);
            var cityHotel = cityHotels.get(i);
            if (localHotel.getId() != cityHotel.getId()) {
                return true;  // Se l'ID di un hotel differisce, i rank locali sono cambiati
            }
        }
        return false;  // Nessun cambiamento nel ranking
    }

    /**
     * Calcola il rank globale di un hotel in base alla qualità, quantità e attualità delle recensioni.
     *
     * @param hotel L'hotel di cui calcolare il rank.
     * @return Il rank globale dell'hotel (compreso tra 1 e 5).
     */
    private double calculateRank(Hotel hotel) {
        // Ottieni la lista delle recensioni per l'hotel
        var reviews = reviewRegister.getHotelReviews(hotel);

        // Calcola la media dei giorni trascorsi dalla pubblicazione di ciascuna recensione
        int totalDays = 0;
        for (Recensioni review : reviews) {
            var timestamp = LocalDateTime.parse(review.getTimestamp());
            totalDays += ChronoUnit.DAYS.between(timestamp, LocalDateTime.now());
        }

        // Calcola la media dei giorni trascorsi
        int reviewCount = hotel.getReviewCount();
        double avgDays = reviewCount > 0 ? (double) totalDays / reviewCount : 0;

        // Calcola il peso dei giorni (più recente è la recensione, maggiore è il peso)
        double pesoGiorni = Math.max(0.5, 1 - avgDays / 100);

        // Ottieni la media delle valutazioni dell'hotel
        double rate = hotel.getRate();

        // Calcola il rank globale dell'hotel
        double rank = (rate * pesoGiorni) + (reviewCount / 2.0);

        // Assicura che il rank sia compreso tra 1 e 5
        return Math.min(5, Math.max(1, rank));
    }

    /**
     * Esegue una sleep per un determinato numero di secondi.
     *
     * @param rankingInterval L'intervallo in secondi tra i calcoli del ranking.
     */
    private void sleep(int rankingInterval) {
        try {
            Thread.sleep(rankingInterval * 1000L);  // Pausa per l'intervallo definito
        } catch (InterruptedException e) {
            e.printStackTrace();  // Gestisce eventuali interruzioni del thread
        }
    }
}
