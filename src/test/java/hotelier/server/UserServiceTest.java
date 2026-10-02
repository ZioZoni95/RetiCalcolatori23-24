package hotelier.server;

import hotelier.model.Badge;
import hotelier.model.User;
import hotelier.util.JsonStore;
import hotelier.util.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @TempDir
    Path dir;

    @Test
    void registerAndAuthenticate() throws IOException {
        var users = new UserService(dir.resolve("Users.json"));
        assertTrue(users.register("Mario", "Pw1").ok());

        User user = users.find("mario").orElseThrow();
        assertTrue(users.passwordMatches(user, "Pw1"));
        assertFalse(users.passwordMatches(user, "pw1"), "la password è case sensitive");
        assertEquals(Badge.REVIEWER, user.badgeLevel());
    }

    @Test
    void rejectsDuplicatesAndInvalidValues() throws IOException {
        var users = new UserService(dir.resolve("Users.json"));
        assertTrue(users.register("mario", "pw").ok());
        assertFalse(users.register("MARIO", "altra").ok());
        assertFalse(users.register("", "pw").ok());
        assertFalse(users.register("luigi", "").ok());
        assertFalse(users.register("lu igi", "pw").ok());
        assertFalse(users.register("luigi", "p w").ok());
        assertFalse(users.register(null, "pw").ok());
    }

    @Test
    void persistsAcrossRestarts() throws IOException {
        Path file = dir.resolve("Users.json");
        var users = new UserService(file);
        users.register("mario", "pw");
        users.recordReview("mario");
        users.recordReview("mario");

        User reloaded = new UserService(file).find("mario").orElseThrow();
        assertEquals(2, reloaded.reviewCount());
        assertEquals(Badge.EXPERT_REVIEWER, reloaded.badgeLevel());
        assertFalse(Files.readString(file).contains("\"pw\""), "la password non va salvata in chiaro");
    }

    @Test
    void legacyPlainTextFilesAreMigrated() throws IOException {
        Path file = dir.resolve("Users.json");
        Files.writeString(file, """
                [ {"badgeLevel":"Super_Contribuente","username":"ciao","password":"ciao","reviewCount":10} ]""");

        var users = new UserService(file);
        User user = users.find("ciao").orElseThrow();
        assertTrue(users.passwordMatches(user, "ciao"));
        assertEquals(10, user.reviewCount());
        assertEquals(Badge.SUPER_CONTRIBUTOR, user.badgeLevel());

        User onDisk = JsonStore.read(file, User[].class)[0];
        assertTrue(PasswordHasher.isHashed(onDisk.password()));
    }
}
