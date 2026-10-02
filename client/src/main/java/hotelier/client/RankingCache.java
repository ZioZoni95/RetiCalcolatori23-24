package hotelier.client;

import hotelier.model.CityRanking;
import hotelier.rmi.ClientCallback;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Ultime classifiche locali ricevute dal server per le città di interesse (callback RMI). */
final class RankingCache implements ClientCallback {

    private final Map<String, CityRanking> rankings = new ConcurrentHashMap<>();

    @Override
    public void rankingChanged(CityRanking ranking) {
        rankings.put(ranking.city(), ranking);
    }

    List<CityRanking> snapshot() {
        Collection<CityRanking> values = rankings.values();
        return values.stream().sorted(Comparator.comparing(CityRanking::city)).toList();
    }

    void clear() {
        rankings.clear();
    }
}
