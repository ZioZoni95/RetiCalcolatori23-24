package hotelier.client;

import hotelier.model.CityRanking;
import hotelier.rmi.ClientCallback;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** Ultime classifiche locali ricevute dal server per le città di interesse (callback RMI). */
final class RankingCache implements ClientCallback {

    private final Map<String, CityRanking> rankings = new ConcurrentHashMap<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void rankingChanged(CityRanking ranking) {
        rankings.put(ranking.city(), ranking);
        listeners.forEach(Runnable::run);
    }

    List<CityRanking> snapshot() {
        return rankings.values().stream().sorted(Comparator.comparing(CityRanking::city)).toList();
    }

    void clear() {
        rankings.clear();
    }

    /** Il listener viene invocato in un thread RMI, non nel thread dell'interfaccia. */
    void addListener(Runnable listener) {
        listeners.add(listener);
    }
}
