package hotelier.server;

import hotelier.model.CityRanking;
import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.util.JsonStore;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Archivio degli hotel, persistito su file JSON. Gli {@link Hotel} sono immutabili, quindi
 * quelli restituiti dai metodi possono essere usati liberamente dai chiamanti.
 */
public final class HotelRepository {

    /** Nuovo ranking di un hotel. */
    public record Ranking(double rank, int localRank) {
    }

    private static final System.Logger LOG = System.getLogger(HotelRepository.class.getName());

    private final Path file;
    private final Map<Integer, Hotel> hotels = new LinkedHashMap<>();

    public HotelRepository(Path file) throws IOException {
        this.file = file;
        for (Hotel hotel : JsonStore.read(file, Hotel[].class)) {
            hotels.put(hotel.id(), hotel);
        }
    }

    public synchronized List<Hotel> all() {
        return List.copyOf(hotels.values());
    }

    public synchronized Optional<Hotel> findByNameAndCity(String name, String city) {
        return hotels.values().stream()
                .filter(h -> h.city().equalsIgnoreCase(city) && h.name().equalsIgnoreCase(name))
                .findFirst();
    }

    /** Hotel della città (senza distinzione tra maiuscole e minuscole), in ordine di posizione locale. */
    public synchronized List<Hotel> findByCity(String city) {
        return hotels.values().stream()
                .filter(h -> h.city().equalsIgnoreCase(city))
                .sorted(Comparator.comparingInt(Hotel::localRank).thenComparingInt(Hotel::id))
                .toList();
    }

    public synchronized Optional<CityRanking> cityRanking(String city) {
        List<Hotel> inCity = findByCity(city);
        return inCity.isEmpty() ? Optional.empty() : Optional.of(new CityRanking(inCity.get(0).city(), inCity));
    }

    /** Aggiorna le statistiche dell'hotel con una nuova recensione e salva su disco. */
    public synchronized Hotel addReview(int hotelId, int rate, Ratings ratings) {
        Hotel updated = hotels.get(hotelId).withReview(rate, ratings);
        hotels.put(hotelId, updated);
        persist();
        return updated;
    }

    /** Applica i nuovi ranking; salva su disco solo se qualcosa è cambiato. */
    public synchronized void updateRankings(Map<Integer, Ranking> rankings) {
        boolean changed = false;
        for (Map.Entry<Integer, Ranking> entry : rankings.entrySet()) {
            Hotel current = hotels.get(entry.getKey());
            Ranking ranking = entry.getValue();
            if (current != null && (current.rank() != ranking.rank() || current.localRank() != ranking.localRank())) {
                hotels.put(current.id(), current.withRanking(ranking.rank(), ranking.localRank()));
                changed = true;
            }
        }
        if (changed) {
            persist();
        }
    }

    private void persist() {
        try {
            JsonStore.write(file, new ArrayList<>(hotels.values()));
        } catch (IOException e) {
            LOG.log(System.Logger.Level.ERROR, "Impossibile salvare " + file, e);
        }
    }
}
