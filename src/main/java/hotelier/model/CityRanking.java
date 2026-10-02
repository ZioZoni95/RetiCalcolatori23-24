package hotelier.model;

import java.io.Serializable;
import java.util.List;

/** Classifica locale di una città: gli hotel in ordine di posizione. */
public record CityRanking(String city, List<Hotel> hotels) implements Serializable {

    public CityRanking {
        hotels = List.copyOf(hotels);
    }
}
