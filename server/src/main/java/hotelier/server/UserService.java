package hotelier.server;

import hotelier.model.User;
import hotelier.util.JsonStore;
import hotelier.util.PasswordHasher;
import hotelier.util.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Utenti registrati, persistiti su file JSON. Gli username sono univoci senza distinzione tra
 * maiuscole e minuscole; le password sono confrontate esattamente.
 */
public final class UserService {

    private static final System.Logger LOG = System.getLogger(UserService.class.getName());

    private final Path file;
    private final Map<String, User> users = new LinkedHashMap<>();

    /** Carica gli utenti; password in chiaro di vecchi file vengono convertite in hash. */
    public UserService(Path file) throws IOException {
        this.file = file;
        boolean migrated = false;
        if (Files.exists(file)) {
            for (User user : JsonStore.read(file, User[].class)) {
                if (!PasswordHasher.isHashed(user.password())) {
                    user = user.withPassword(PasswordHasher.hash(user.password()));
                    migrated = true;
                }
                users.put(key(user.username()), user);
            }
        } else {
            migrated = true;
        }
        if (migrated) {
            JsonStore.write(file, new ArrayList<>(users.values()));
        }
    }

    public Result register(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            return Result.failure("Errore: Username e Password non possono essere vuoti");
        }
        if (containsWhitespace(username) || containsWhitespace(password)) {
            return Result.failure("Errore Caratteri: Spazi vuoti non permessi in Username e Password");
        }
        String hash = PasswordHasher.hash(password);
        synchronized (this) {
            if (users.containsKey(key(username))) {
                return Result.failure("Errore Registrazione: Utente già presente");
            }
            users.put(key(username), User.create(username, hash));
            persist();
        }
        return Result.success("Nuovo Utente : " + username + " è stato registrato con successo");
    }

    public synchronized List<User> all() {
        return List.copyOf(users.values());
    }

    public synchronized Optional<User> find(String username) {
        return Optional.ofNullable(users.get(key(username)));
    }

    public boolean passwordMatches(User user, String password) {
        return PasswordHasher.verify(password, user.password());
    }

    /** Conta una nuova recensione per l'utente e ne aggiorna il badge. */
    public synchronized User recordReview(String username) {
        User updated = users.get(key(username)).withNewReview();
        users.put(key(username), updated);
        persist();
        return updated;
    }

    private void persist() {
        try {
            JsonStore.write(file, new ArrayList<>(users.values()));
        } catch (IOException e) {
            LOG.log(System.Logger.Level.ERROR, "Impossibile salvare " + file, e);
        }
    }

    private static String key(String username) {
        return username.toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean containsWhitespace(String value) {
        return value.chars().anyMatch(Character::isWhitespace);
    }
}
