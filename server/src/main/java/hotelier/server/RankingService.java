package hotelier.server;

import hotelier.model.CityRanking;
import hotelier.model.Hotel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Ricalcola periodicamente il ranking globale e locale degli hotel, lo salva e notifica:
 * <ul>
 *   <li>via multicast, quando cambia il primo hotel di una città;</li>
 *   <li>via callback RMI, quando cambia l'ordine della classifica di una città.</li>
 * </ul>
 */
public final class RankingService implements Runnable {

    private static final System.Logger LOG = System.getLogger(RankingService.class.getName());

    private static final Comparator<Hotel> BY_RANK =
            Comparator.comparingDouble(Hotel::rank).reversed().thenComparingInt(Hotel::id);

    private final HotelRepository hotels;
    private final ReviewService reviews;
    private final Consumer<Hotel> firstPlaceListener;
    private final Consumer<CityRanking> rankingListener;
    private final Map<String, List<Integer>> previousOrder = new HashMap<>();

    public RankingService(HotelRepository hotels, ReviewService reviews,
                          Consumer<Hotel> firstPlaceListener, Consumer<CityRanking> rankingListener) {
        this.hotels = hotels;
        this.reviews = reviews;
        this.firstPlaceListener = firstPlaceListener;
        this.rankingListener = rankingListener;
        previousOrder.putAll(orderByCity(hotels.all()));
    }

    @Override
    public void run() {
        try {
            update(LocalDateTime.now());
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "Errore nel calcolo del ranking", e);
        }
    }

    /** Un ciclo di calcolo. Visibile ai test. */
    void update(LocalDateTime now) {
        List<Hotel> ranked = new ArrayList<>();
        for (Hotel hotel : hotels.all()) {
            double rank = RankCalculator.rank(hotel, reviews.forHotel(hotel.id()), now);
            ranked.add(hotel.withRanking(rank, hotel.localRank()));
        }

        Map<Integer, HotelRepository.Ranking> rankings = new HashMap<>();
        Map<String, List<Hotel>> cities = groupByCity(ranked);
        for (List<Hotel> inCity : cities.values()) {
            inCity.sort(BY_RANK);
            for (int i = 0; i < inCity.size(); i++) {
                Hotel hotel = inCity.get(i);
                rankings.put(hotel.id(), new HotelRepository.Ranking(hotel.rank(), i + 1));
            }
        }
        hotels.updateRankings(rankings);

        for (Map.Entry<String, List<Hotel>> entry : cities.entrySet()) {
            String city = entry.getKey();
            List<Hotel> current = entry.getValue().stream()
                    .map(h -> h.withRanking(h.rank(), rankings.get(h.id()).localRank()))
                    .toList();
            List<Integer> order = current.stream().map(Hotel::id).toList();
            List<Integer> previous = previousOrder.put(city, order);
            if (previous == null || previous.equals(order)) {
                continue;
            }
            if (!previous.get(0).equals(order.get(0))) {
                firstPlaceListener.accept(current.get(0));
            }
            rankingListener.accept(new CityRanking(city, current));
        }
    }

    private static Map<String, List<Integer>> orderByCity(List<Hotel> all) {
        Map<String, List<Integer>> result = new HashMap<>();
        groupByCity(all).forEach((city, inCity) -> {
            inCity.sort(BY_RANK);
            result.put(city, inCity.stream().map(Hotel::id).toList());
        });
        return result;
    }

    private static Map<String, List<Hotel>> groupByCity(List<Hotel> all) {
        Map<String, List<Hotel>> cities = new LinkedHashMap<>();
        for (Hotel hotel : all) {
            cities.computeIfAbsent(hotel.city(), c -> new ArrayList<>()).add(hotel);
        }
        return cities;
    }
}
