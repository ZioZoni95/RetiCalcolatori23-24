package hotelier.client;

import hotelier.config.ClientConfig;
import hotelier.util.JsonStore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Avvio del client. Uso: {@code java -cp hotelier.jar hotelier.client.ClientMain [cartella-config]}
 * (default: {@code resources}).
 */
public final class ClientMain {

    private ClientMain() {
    }

    public static void main(String[] args) throws Exception {
        Path dir = Path.of(args.length > 0 ? args[0] : "resources");
        ClientConfig config = JsonStore.readOrCreate(dir.resolve("ClientConfig.json"), ClientConfig.class,
                ClientConfig.defaults());

        TcpClient tcp = null;
        RmiClient rmi = null;
        MulticastListener multicast = null;
        try {
            tcp = new TcpClient(config.serverAddress(), config.tcpPort());
            rmi = new RmiClient(config.serverAddress(), config.rmiPort(), config.rmiRemoteReference());
            multicast = new MulticastListener(config.mcastAddress(), config.mcastPort(),
                    text -> System.out.println("\n<Notifica Ricevuta:> " + text + "\n"));
        } catch (Exception e) {
            System.err.println("<Errore> Impossibile contattare il server di Hotelier: " + e);
            close(tcp, rmi, multicast);
            System.exit(1);
        }

        try (var in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            new Cli(tcp, rmi, multicast, in, System.out).run();
        } finally {
            close(tcp, rmi, multicast);
        }
        // i thread RMI non sono daemon: l'uscita esplicita evita che il processo resti attivo
        System.exit(0);
    }

    private static void close(TcpClient tcp, RmiClient rmi, MulticastListener multicast) {
        if (multicast != null) {
            multicast.close();
        }
        if (rmi != null) {
            rmi.close();
        }
        if (tcp != null) {
            tcp.close();
        }
    }
}
