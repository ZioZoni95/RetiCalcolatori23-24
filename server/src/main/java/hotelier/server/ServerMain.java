package hotelier.server;

import hotelier.server.ui.PlainServerUi;
import hotelier.server.ui.ServerGui;
import hotelier.server.ui.ServerTui;
import hotelier.util.LaunchOptions;

/**
 * Avvio del server.
 * Uso: {@code java -jar hotelier-server.jar [--ui headless|tui|gui] [cartella-dati]}
 * (cartella di default: {@code data/server}; interfaccia di default: la migliore disponibile).
 */
public final class ServerMain {

    private ServerMain() {
    }

    public static void main(String[] args) {
        LaunchOptions options;
        try {
            options = LaunchOptions.parse(args, "data/server");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("Uso: hotelier-server [--ui headless|tui|gui] [cartella-dati]");
            System.exit(2);
            return;
        }
        try {
            switch (options.mode()) {
                case PLAIN -> PlainServerUi.run(options.dir());
                case TUI -> {
                    ServerTui.run(options.dir());
                    System.exit(0); // alla chiusura della TUI il server è già stato fermato
                }
                case GUI -> ServerGui.run(options.dir());
            }
        } catch (Exception e) {
            System.err.println("[ERRORE] Impossibile avviare il server: " + e);
            System.exit(1);
        }
    }
}
