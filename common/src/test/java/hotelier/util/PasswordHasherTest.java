package hotelier.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void verifiesOnlyTheRightPasswordCaseSensitively() {
        String hash = PasswordHasher.hash("Segreta1");
        assertTrue(PasswordHasher.isHashed(hash));
        assertTrue(PasswordHasher.verify("Segreta1", hash));
        assertFalse(PasswordHasher.verify("segreta1", hash));
        assertFalse(PasswordHasher.verify("altra", hash));
    }

    @Test
    void saltIsDifferentEachTime() {
        assertNotEquals(PasswordHasher.hash("x"), PasswordHasher.hash("x"));
    }

    @Test
    void plainTextIsNeverAccepted() {
        assertFalse(PasswordHasher.isHashed("ciao"));
        assertFalse(PasswordHasher.verify("ciao", "ciao"));
    }
}
