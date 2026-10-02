package hotelier.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Utente registrato. Il campo {@code password} contiene l'hash PBKDF2 (vedi
 * {@link hotelier.util.PasswordHasher}), mai la password in chiaro.
 */
@JsonPropertyOrder({"badgeLevel", "username", "password", "reviewCount"})
public record User(String username, String password, int reviewCount, Badge badgeLevel) {

    public static User create(String username, String passwordHash) {
        return new User(username, passwordHash, 0, Badge.REVIEWER);
    }

    public User withPassword(String passwordHash) {
        return new User(username, passwordHash, reviewCount, badgeLevel);
    }

    /** L'utente dopo una nuova recensione, con il badge aggiornato. */
    public User withNewReview() {
        int count = reviewCount + 1;
        return new User(username, password, count, Badge.forReviewCount(count));
    }

    @Override
    public String toString() {
        return "User[username=" + username + ", reviewCount=" + reviewCount + ", badgeLevel=" + badgeLevel + "]";
    }
}
