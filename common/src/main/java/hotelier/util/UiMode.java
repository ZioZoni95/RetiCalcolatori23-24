package hotelier.util;

import java.awt.GraphicsEnvironment;
import java.util.Locale;

/** Interfaccia utente con cui avviare client o server. */
public enum UiMode {
    /** Riga di comando (client) o solo log su console (server). */
    PLAIN,
    /** Interfaccia testuale a schermo intero nel terminale. */
    TUI,
    /** Interfaccia grafica Swing. */
    GUI;

    public static UiMode parse(String text) {
        return switch (text.toLowerCase(Locale.ROOT)) {
            case "plain", "cli", "headless" -> PLAIN;
            case "tui" -> TUI;
            case "gui" -> GUI;
            default -> throw new IllegalArgumentException(
                    "Interfaccia sconosciuta: " + text + " (valori ammessi: cli/headless, tui, gui)");
        };
    }

    /** GUI se c'è uno schermo, altrimenti TUI se c'è un terminale interattivo, altrimenti PLAIN. */
    public static UiMode detect() {
        if (!GraphicsEnvironment.isHeadless()) {
            return GUI;
        }
        return System.console() != null ? TUI : PLAIN;
    }
}
