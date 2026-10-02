package hotelier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/** Valutazioni parziali (0-5) di un hotel, o medie di più recensioni. */
public record Ratings(double cleaning, double position, double services, double quality) implements Serializable {

    public static final Ratings ZERO = new Ratings(0, 0, 0, 0);

    /** Media corrente aggiornata con una nuova valutazione, dato il numero di recensioni già incluse. */
    public Ratings runningAverage(Ratings added, int previousCount) {
        return new Ratings(
                average(cleaning, added.cleaning, previousCount),
                average(position, added.position, previousCount),
                average(services, added.services, previousCount),
                average(quality, added.quality, previousCount));
    }

    /** Media dei quattro criteri. */
    public double mean() {
        return (cleaning + position + services + quality) / 4;
    }

    /** Vero se tutti i criteri sono compresi tra 0 e 5. */
    @JsonIgnore
    public boolean isValid() {
        return inRange(cleaning) && inRange(position) && inRange(services) && inRange(quality);
    }

    private static boolean inRange(double value) {
        return value >= 0 && value <= 5;
    }

    private static double average(double current, double added, int count) {
        return (current * count + added) / (count + 1);
    }
}
