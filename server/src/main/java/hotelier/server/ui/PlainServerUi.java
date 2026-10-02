package hotelier.server.ui;

import hotelier.server.ServerRuntime;

import java.nio.file.Path;

/** Server senza interfaccia: gli eventi vengono stampati sulla console fino alla chiusura (Ctrl+C). */
public final class PlainServerUi {

    private PlainServerUi() {
    }

    public static void run(Path dataDir) throws Exception {
        ServerRuntime server = ServerRuntime.start(dataDir);
        server.events().addListener(entry -> System.out.println(entry));
        Runtime.getRuntime().addShutdownHook(new Thread(server::close, "shutdown"));
        System.out.println("Server avviato (Ctrl+C per fermarlo). Eventi:");
        server.events().recent().forEach(System.out::println);
        Thread.currentThread().join();
    }
}
