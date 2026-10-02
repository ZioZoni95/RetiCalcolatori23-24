package hotelier.client;

import hotelier.client.ui.ClientGui;
import hotelier.client.ui.ClientTui;
import hotelier.client.ui.PlainClientUi;
import hotelier.config.ClientConfig;
import hotelier.util.JsonStore;
import hotelier.util.LaunchOptions;
import hotelier.util.UiMode;

/**
 * Avvio del client.
 * Uso: {@code java -jar hotelier-client.jar [--ui cli|tui|gui] [cartella-config]}
 * (cartella di default: {@code data/client}; interfaccia di default: la migliore disponibile).
 */
public final class ClientMain {

    private ClientMain() {
    }

    public static void main(String[] args) throws Exception {
        LaunchOptions options;
        try {
            options = LaunchOptions.parse(args, "data/client");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("Uso: hotelier-client [--ui cli|tui|gui] [cartella-config]");
            System.exit(2);
            return;
        }

        ClientConfig config = JsonStore.readOrCreate(options.dir().resolve("ClientConfig.json"), ClientConfig.class,
                ClientConfig.defaults());
        HotelierClient client;
        try {
            client = HotelierClient.connect(config);
        } catch (Exception e) {
            String message = "Impossibile contattare il server di Hotelier (" + config.serverAddress() + "): " + e;
            if (options.mode() == UiMode.GUI) {
                ClientGui.showFatalError(message);
            }
            System.err.println("<Errore> " + message);
            System.exit(1);
            return;
        }

        switch (options.mode()) {
            case PLAIN -> PlainClientUi.run(client);
            case TUI -> ClientTui.run(client);
            case GUI -> {
                ClientGui.run(client);
                return; // il processo termina alla chiusura della finestra
            }
        }
        client.close();
        // i thread RMI non sono daemon: l'uscita esplicita evita che il processo resti attivo
        System.exit(0);
    }
}
