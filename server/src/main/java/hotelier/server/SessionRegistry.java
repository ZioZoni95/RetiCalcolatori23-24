package hotelier.server;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Utenti attualmente collegati: un utente può avere una sola sessione attiva. */
public final class SessionRegistry {

    private final Map<String, String> loggedIn = new ConcurrentHashMap<>();

    /** Vero se l'utente non era già collegato. */
    public boolean tryLogin(String username) {
        return loggedIn.putIfAbsent(key(username), username) == null;
    }

    public void logout(String username) {
        loggedIn.remove(key(username));
    }

    /** Gli utenti collegati, in ordine alfabetico. */
    public List<String> users() {
        return loggedIn.values().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    private static String key(String username) {
        return username.toLowerCase(Locale.ROOT);
    }
}
