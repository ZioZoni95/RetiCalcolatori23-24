package hotelier.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Livello di un utente, in funzione del numero di recensioni pubblicate. */
public enum Badge {
    @JsonProperty("Recensore") REVIEWER("Recensore", 0),
    @JsonProperty("Recensore_Esperto") EXPERT_REVIEWER("Recensore Esperto", 2),
    @JsonProperty("Contribuente") CONTRIBUTOR("Contribuente", 3),
    @JsonProperty("Contribuente_Esperto") EXPERT_CONTRIBUTOR("Contribuente Esperto", 4),
    @JsonProperty("Super_Contribuente") SUPER_CONTRIBUTOR("Super Contribuente", 5);

    private final String displayName;
    private final int minReviews;

    Badge(String displayName, int minReviews) {
        this.displayName = displayName;
        this.minReviews = minReviews;
    }

    public String displayName() {
        return displayName;
    }

    /** Il badge più alto raggiunto con {@code reviewCount} recensioni. */
    public static Badge forReviewCount(int reviewCount) {
        Badge result = REVIEWER;
        for (Badge badge : values()) {
            if (reviewCount >= badge.minReviews) {
                result = badge;
            }
        }
        return result;
    }
}
