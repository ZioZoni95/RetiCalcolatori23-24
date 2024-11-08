package server;

import Response_Request_netPackets.Request_ResponseMessage;
import server.Handlers.HotelierServerConnectionClientHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * La classe HotelierNIOServerTCP implementa un server TCP basato su NIO (Non-blocking I/O) utilizzando
 * i canali e i selettori di Java NIO per gestire le connessioni client in modo non bloccante e altamente concorrente.
 * Questo server è progettato per gestire molteplici connessioni simultanee senza creare un thread per ogni client,
 * utilizzando il modello di selezione (selector pattern).
 */
public class HotelierNIOServerTCP implements Runnable {
    private final String serverAddress;
    private final int nioTCPPort;
    private ExecutorService threadpool;

    /**
     * Costruttore della classe HotelierNIOServerTCP.
     *
     * @param serverAddress L'indirizzo IP o il nome host del server.
     * @param nioTCPPort La porta su cui il server ascolterà le connessioni in ingresso.
     */
    public HotelierNIOServerTCP(String serverAddress, int nioTCPPort) {
        this.serverAddress = serverAddress;
        this.nioTCPPort = nioTCPPort;
        // Creazione di un thread pool con un numero di thread dinamico
        threadpool = Executors.newCachedThreadPool();
        // Avvio di un nuovo thread per eseguire il server
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Metodo che avvia il server TCP e gestisce le connessioni client in modo non bloccante
     * utilizzando Java NIO (Non-blocking I/O).
     */
    @Override
    public void run() {
        try {
            // Configura l'indirizzo del server su cui il server NIO ascolterà
            InetSocketAddress serverSocketAddress = new InetSocketAddress(serverAddress, nioTCPPort);

            // Apre un ServerSocketChannel per accettare le connessioni in ingresso e lo configura come non-bloccante
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);

            // Associa il ServerSocketChannel all'indirizzo e alla porta specificati
            serverSocketChannel.bind(serverSocketAddress);

            // Crea un selector per monitorare più canali (connessioni)
            Selector serverSelector = Selector.open();
            // Registra il canale del server per l'operazione di accettazione delle connessioni in ingresso
            serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);

            // Ciclo principale del server che continua finché il thread non viene interrotto
            while (!Thread.interrupted()) {
                // Seleziona i canali pronti per operazioni I/O (ad esempio, connessioni in ingresso)
                serverSelector.select();
                Set<SelectionKey> keys = serverSelector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();

                // Itera attraverso le chiavi selezionate (canali pronti)
                while (iter.hasNext()) {
                    // Ottiene la chiave selezionata
                    SelectionKey keySelected = iter.next();

                    // Gestisce le connessioni in ingresso (nuovi client)
                    if (keySelected.isAcceptable()) {
                        // Accetta una connessione in ingresso dal ServerSocketChannel
                        SocketChannel client = serverSocketChannel.accept();
                        // Configura il canale del client come non-bloccante
                        client.configureBlocking(false);

                        // Registra il canale del client per le operazioni di lettura e scrittura
                        SelectionKey keyOfClient = client.register(serverSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        // Associa un handler (gestore della connessione) al canale del client
                        keyOfClient.attach(new HotelierServerConnectionClientHandler(client));
                    }

                    // Gestisce eventi di lettura dai client
                    if (keySelected.isValid() && keySelected.isReadable()) {
                        // Ottiene il gestore della connessione del client
                        HotelierServerConnectionClientHandler clientHandler = (HotelierServerConnectionClientHandler) keySelected.attachment();
                        // Gestisce la lettura del pacchetto in ingresso dal client
                        Request_ResponseMessage packet = clientHandler.handleRead();

                        // Se un pacchetto è stato letto, invialo al thread pool per l'elaborazione
                        if (packet != null) {
                            threadpool.submit(() -> {
                                // Gestisce il pacchetto (elaborazione richiesta)
                                clientHandler.handlePacket(packet);
                            });
                        }

                        // Se la connessione del client è stata chiusa, chiudi il canale
                        if (!clientHandler.isConnected()) {
                            clientHandler.close();
                            keySelected.cancel(); // Cancella la chiave dal selector
                        }
                    }

                    // Gestisce eventi di scrittura verso i client
                    if (keySelected.isValid() && keySelected.isWritable()) {
                        // Ottiene il gestore della connessione del client
                        HotelierServerConnectionClientHandler clientHandler = (HotelierServerConnectionClientHandler) keySelected.attachment();
                        // Gestisce la scrittura dei dati al client
                        clientHandler.handleWrite();
                        // Se la connessione del client è stata chiusa, chiudi il canale
                        if (!clientHandler.isConnected()) {
                            clientHandler.close();
                            keySelected.cancel(); // Cancella la chiave dal selector
                        }
                    }
                    // Rimuove la chiave dalla lista delle chiavi selezionate
                    iter.remove();
                }
            }
        } catch (IOException e) {
            // Gestisce eventuali eccezioni durante la selezione o la gestione dei canali
            e.printStackTrace();
        }
    }
}
