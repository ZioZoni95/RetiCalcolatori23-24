package hotelier.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server TCP basato su NIO: un solo thread con un {@link Selector} gestisce tutte le connessioni,
 * le richieste vengono elaborate da un pool di worker.
 */
public final class NioServer implements Closeable {

    private static final System.Logger LOG = System.getLogger(NioServer.class.getName());

    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final Services services;
    private final AtomicInteger connections = new AtomicInteger();
    private final ExecutorService workers = Executors.newCachedThreadPool();
    private final Thread thread = new Thread(this::loop, "nio-server");

    /** Apre la porta (0 = una libera a scelta); le richieste vengono accettate dopo {@link #start()}. */
    public NioServer(String host, int port, Services services) throws IOException {
        this.services = services;
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        try {
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(host, port));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public int port() throws IOException {
        return ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();
    }

    /** Numero di client attualmente connessi. */
    public int connectionCount() {
        return connections.get();
    }

    public void start() {
        thread.start();
    }

    private void loop() {
        try {
            while (selector.isOpen()) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    dispatch(key);
                }
            }
        } catch (ClosedSelectorException e) {
            // chiusura richiesta
        } catch (IOException e) {
            LOG.log(System.Logger.Level.ERROR, "Errore nel selector, il server TCP si ferma", e);
        }
    }

    private void dispatch(SelectionKey key) {
        try {
            if (key.isAcceptable()) {
                accept();
                return;
            }
            Connection connection = (Connection) key.attachment();
            if (key.isValid() && key.isReadable()) {
                connection.onReadable();
            }
            if (key.isValid() && key.isWritable()) {
                connection.onWritable();
            }
        } catch (IOException | CancelledKeyException e) {
            if (key.attachment() instanceof Connection connection) {
                connection.close();
            }
        }
    }

    private void accept() throws IOException {
        SocketChannel client = serverChannel.accept();
        if (client == null) {
            return;
        }
        client.configureBlocking(false);
        SelectionKey key = client.register(selector, SelectionKey.OP_READ);
        String remote = String.valueOf(client.getRemoteAddress());
        connections.incrementAndGet();
        services.events().log("Client connesso: " + remote);
        key.attach(new Connection(client, key, new Session(services), workers, () -> {
            connections.decrementAndGet();
            services.events().log("Client disconnesso: " + remote);
        }));
    }

    @Override
    public void close() {
        try {
            for (SelectionKey key : selector.keys()) {
                if (key.attachment() instanceof Connection connection) {
                    connection.close();
                }
            }
            selector.close();
        } catch (IOException | ClosedSelectorException e) {
            // in chiusura
        }
        try {
            serverChannel.close();
        } catch (IOException e) {
            // in chiusura
        }
        workers.shutdownNow();
    }
}
