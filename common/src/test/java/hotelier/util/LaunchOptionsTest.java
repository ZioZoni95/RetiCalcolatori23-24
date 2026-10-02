package hotelier.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LaunchOptionsTest {

    @Test
    void parsesModeAndDirectory() {
        var options = LaunchOptions.parse(new String[]{"--ui", "tui", "dati"}, "data/server");
        assertEquals(UiMode.TUI, options.mode());
        assertEquals(Path.of("dati"), options.dir());
    }

    @Test
    void defaultsAndEqualsForm() {
        var options = LaunchOptions.parse(new String[]{"--ui=GUI"}, "data/client");
        assertEquals(UiMode.GUI, options.mode());
        assertEquals(Path.of("data/client"), options.dir());
        assertNotNull(LaunchOptions.parse(new String[0], "x").mode());
    }

    @Test
    void cliAndHeadlessAreTheSameMode() {
        assertEquals(UiMode.PLAIN, UiMode.parse("cli"));
        assertEquals(UiMode.PLAIN, UiMode.parse("headless"));
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> LaunchOptions.parse(new String[]{"--ui"}, "x"));
        assertThrows(IllegalArgumentException.class, () -> LaunchOptions.parse(new String[]{"--ui", "web"}, "x"));
        assertThrows(IllegalArgumentException.class, () -> LaunchOptions.parse(new String[]{"--boh"}, "x"));
        assertThrows(IllegalArgumentException.class, () -> LaunchOptions.parse(new String[]{"a", "b"}, "x"));
    }
}
