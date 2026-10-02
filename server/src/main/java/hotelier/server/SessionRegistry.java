package hotelier.server;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Utenti attualmente collegati: un utente può avere una sola sessione attiva. */
public final class SessionRegistry {

    private final Set<String> loggedIn = ConcurrentHashMap.newKeySet();

    /** Vero se l'utente non era già collegato. */
    public boolean tryLogin(String username) {
        return loggedIn.add(key(username));
    }

    public void logout(String username) {
        loggedIn.remove(key(username));
    }

    private static String key(String username) {
        return username.toLowerCase(Locale.ROOT);
    }
}
