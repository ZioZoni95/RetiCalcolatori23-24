package hotelier.util;

import java.nio.file.Path;

/**
 * Argomenti di avvio comuni a client e server: {@code [--ui cli|headless|tui|gui] [cartella]}.
 * Senza {@code --ui} l'interfaccia viene scelta con {@link UiMode#detect()}.
 */
public record LaunchOptions(UiMode mode, Path dir) {

    public static LaunchOptions parse(String[] args, String defaultDir) {
        UiMode mode = null;
        Path dir = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--ui")) {
                if (++i >= args.length) {
                    throw new IllegalArgumentException("--ui richiede un valore");
                }
                mode = UiMode.parse(args[i]);
            } else if (arg.startsWith("--ui=")) {
                mode = UiMode.parse(arg.substring("--ui=".length()));
            } else if (arg.startsWith("--")) {
                throw new IllegalArgumentException("Opzione sconosciuta: " + arg);
            } else if (dir == null) {
                dir = Path.of(arg);
            } else {
                throw new IllegalArgumentException("Argomento inatteso: " + arg);
            }
        }
        return new LaunchOptions(mode != null ? mode : UiMode.detect(), dir != null ? dir : Path.of(defaultDir));
    }
}
