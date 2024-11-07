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

    /**
     * La classe HotelierServerRanking gestisce il calcolo e l'aggiornamento periodico dei rank globali e locali degli hotel.
     * Avvia un thread per eseguire ogni rankingInterval secondi le seguenti operazioni:
     * 1. Calcola il rank globale di ogni hotel in base a: numero, qualità e attualità delle sue recensioni.
     * 2. Aggiorna i rank locali degli hotel per ogni città.
     * 3. Serializza e persiste i rank aggioranti degli hotel su disco.
     * 4. In caso di cambiamento della prima posizione del rank locale di una città lo notifica tramite multicast a tutti gli utenti loggati.
     * 5. In caso di cambiamento di rank locale lo notifica tramite callback RMI a tutti gli utenti interessati.
     * L' idea di fondo consiste nel tenere un rank globale per tutti gli hotel e utilizzare quest' ultimo per il calcolo del rank locale
     * degli hotel rispetto alla loro città.
     * Al fine di tenere traccia di: numero, qualità e attualità delle recensioni il calcolo del rank globale di un hotel
     * è stato implementato come segue:
     * 1. Ottiene le recensioni dell' hotel.
     * 2. Calcola la media dei minuti trascorsi dalla pubblicazione delle sue recensioni.
     * 3. Calcola il nuovo rank locale utilizzando la seguente formula:
     *  • log10(1 + numero recensioni) * rate hotel (media dei rate delle sue recensioni) + e^-(media minuti trascorsi/60);
     * 4. Infine viene applicata una penalizzazione (1/10 del rank) se il rate hotel era minore di 2per evitare che hotel
     * con numero elevato di recensioni negative abbiano un rank maggiore di hotel con recensioni positive.
     * Tramite questo algoritmo viene data importanza in ordine a: rate , numero recensioni e attualità.
     */


    // Intervallo in secondi tra i calcoli dei rank
    private final int rankingInterval;
    // server Rmi
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
        // setto rankingInterval a quello passato
        this.rankingInterval = rankingInterval;
        // setto serverRmi a quello passato
        this.serverRmi = serverRmi;
        // setto multicastSender a quello multicastSender
        this.multicastSender = multicastSender;
        // ottengo istanza registro hotel (singletone)
        hotelRegister = HotelierServerHotelManager.getInstance();
        // ottengo istanza registro recensioni (singletone)
        reviewRegister = HotelierServerReviewManager.getInstance();
        // inizializzo localRanks a lista vuota
        localRanks = new ArrayList<>();

        // avvio il thread
        Thread thread = new Thread(this);
        thread.start();
    }


    // inizializza la lista dei rank locali utilizzata per verificare cambiamenti
    private void initializeLocalRanks() {

        // ottengo la lista di tutte le  città degli hotel presenti nel registro
        var cities = hotelRegister.getCities();
        // itero le città
        for (String city : cities) {

            // ottengo la lista di hotel aventi come città city
            var hotels = hotelRegister.getHotelsByCity(city);
            // ordino crescentemente la lista di hotel rispetto al rank locale
            Collections.sort(hotels, Comparator.comparingInt(Hotel::getLocalRank));
            // instanzio un nuovo localRank per città city
            var localRank = new LocalHotelRanking(city);

            // inzializzo la lista di hotel di localRank aggiungendo una copia di ogni hotel presente in hotels,
            // al fine che gli update dei rank non si riflettano su quest' ultima e quindi possa essere usata per il confronto
            for (Hotel hotel : hotels) {
                localRank.add(new Hotel(hotel));
            }

            //aggiungo il localRank alla lista di localRank
            localRanks.add(localRank);
        }
    }

    // esegue aggiornamento dei rank locali di tutti gli hotel presenti nel registro hotel
    private void udpateHotelsLocalRank() {

        // ottengo la lista di tutte le  città degli hotel presenti nel registro
        var cities = hotelRegister.getCities();
        // itero le città
        for (String city : cities) {
            // ottengo la lista di hotel aventi come città city
            var hotels = hotelRegister.getHotelsByCity(city);
            // ordino la lista di hotel in ordine decrescente rispetto al rank globale (hotel con rank globale più alto rispetto
            // agli altri hotel aventi la stessa città avrà rank locale 1, ovvero sarà il primo e così via)
            Collections.sort(hotels, Comparator.comparingDouble(Hotel::getRank).reversed());

            // itero la lista degli hotel ordinato decresentemente per rank globale
            for (int i = 0; i < hotels.size(); i++) {
                // ottengo hotel alla posizione 1
                var hotel = hotels.get(i);
                // aggiorno rank locale a i + 1 (in questo modo i rank locali partono da 1 e non da 0)
                hotel.setLocalRanking(i + 1);
            }
        }
    }

    @Override
    public void run() {

        // inizializzo localRanks
        initializeLocalRanks();

        // itero finchè il thread non viene interrotto
        while (!Thread.interrupted()) {

            // ottengo la lista degli hotel prenseti nel registro
            var hotels = hotelRegister.getHotels();
            // itero la lista di hotel
            for (Hotel hotel : hotels) {
                // controllo che hotel abbia della recensioni
                if (hotel.getReviewCount() != 0) {

                    // hotel ha delle recensioni
                    // calcolo il nuovo rank globale dell' hotel
                    var rank = calculateRank(hotel);
                    // setto il nuovo rank globale dell' hotel
                    hotel.setRankLevel(rank);
                }
            }

            // aggiorno i rank locali di tutti gli hotel presenti nel registro
            udpateHotelsLocalRank();
            // persito la lista di hotel del registro sul disco
            hotelRegister.serialize();

            // itero la lista di localRank
            for (LocalHotelRanking localRank : localRanks) {

                // ottengo la lista di hotel di localRank
                var localRankHotels = localRank.getHotels();
                // ottengo la città di localRank
                var localRankCity = localRank.getCity();
                // ottengo gli hotel dal registro aventi città localRankCity
                var cityHotels = hotelRegister.getHotelsByCity(localRankCity);
                // ordino crescentemente localRankHotels rispetto al rank locale
                Collections.sort(localRankHotels, Comparator.comparingInt(Hotel::getLocalRank));
                // ordino crescentemente cityHotels rispetto al rank locale
                Collections.sort(cityHotels, Comparator.comparingInt(Hotel::getLocalRank));
                // ottengo il hotel in prima posizione di localRankHotels
                var localRankFirstHotel = localRankHotels.getFirst();
                // ottengo il hotel in prima posizione di cityHotels
                var cityHotelsFirstHotel = cityHotels.getFirst();

                // controllo se è avvenuto un cambiamento hotel in prima posizione per il rank locale
                if (localRankFirstHotel.getId() != cityHotelsFirstHotel.getId()) {
                    // notifico in multicast cambiamento hotel prima posizione per il rank locale a tutti i client loggati
                    multicastSender.notifyFirstPosition(cityHotelsFirstHotel);

                }

                // controllo se è cambiato localRank
                if (isLocalRankChanged(localRankHotels, cityHotels)) {

                    // instanzio un nuova lista di hotel
                    List<Hotel> cityHotelsCopy = new ArrayList<>();
                    // Aggiungo a cityHotelsCopy la copia di tutti gli hotel del registro aventi città localRankCity
                    // Anche in questo la copia serve a evitare che gli update dei rank globali e locali si riflettano su localRank
                    for (Hotel hotel : cityHotels) {
                        cityHotelsCopy.add(new Hotel(hotel));
                    }

                    // aggiorno la lista di hotel di localRank a cityHotelsCopy
                    localRank.setHotels(cityHotelsCopy);
                    // notifico tramite callback Rmi prima a tutti in client interessati a local rank il cambiamento avvenuto
                    try {
                        serverRmi.notifyLocalRank(localRank);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // aspetto rankingInterval secondi
            sleep(rankingInterval);

        }

    }

    //Restituisce true se localRank è cambiato (liste con dimensione/ordinamento diverso), false altrimenti
    private boolean isLocalRankChanged(List<Hotel> localRankHotels, List<Hotel> cityHotels) {

        // Controllo se localRankHotels e cityHotels hanno dimensione diversa
        if (localRankHotels.size() != cityHotels.size()) {
            // restituisco true
            return true;
        }

        // Itero localRankHotels
        for (int i = 0; i < localRankHotels.size(); i++) {

            // ottengo hotel alla posizione i di localRankHotels
            var localHotel = localRankHotels.get(i);
            // ottengo hotel alla posizione i di cityHotels
            var cityHotel = cityHotels.get(i);

            // controllo se gli hotel sono diversi, ovvero hanno id diverso (vuol dire che le liste sono ordinate diversamente,
            // ovvero si è verificato un cambiamento
            if (localHotel.getId() != cityHotel.getId()) {
                // restituisco true
                return true;
            }
        }
        // restituisco false
        return false;
    }

    // calcola e restituisce il nuovo rank globale dell' hotel
    private double calculateRank(Hotel hotel) {

        // ottengo la lista di recensioni dell' hotel
        var reviews = reviewRegister.getHotelReviews(hotel);

        // variabile per calcolo media minuti trascorsi
        var totalMinutes = 0;

        // itero la lista delle recensioni dell' hotel
        for (Recensioni review : reviews) {

            // ottengo i minuti passati dalla sua pubblicazione
            var timestamp = LocalDateTime.parse(review.getTimestamp());
            // somma a totalMinutes i minuti passati
            totalMinutes += ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now());

        }

        // ottengo il numero di recensioni dell' hotel
        var reviewCount = hotel.getReviewCount();
        // ottengo il rate dell' hotel (media dei rate delle sue recensioni)
        var rate = hotel.getRate();
        // calcolo i minuti trascorsi in media dalla pubblicazione delle sure recensioni
        var avgMinutes = totalMinutes / reviewCount;

        // calcolo il nuovo rank
        var rank = Math.log10(1 + reviewCount) * rate + Math.exp(-avgMinutes / 60);

        return rank;

    }


    private void sleep(int rankingInterval) {
        try {
            Thread.sleep(rankingInterval * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
