package hotelier.server;

import hotelier.protocol.Message;
import hotelier.protocol.Message.Failure;
import hotelier.protocol.Wire;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * Una connessione client gestita da {@link NioServer}. La lettura e la scrittura avvengono nel thread
 * del selector; l'elaborazione delle richieste in un worker, ma una richiesta alla volta e nell'ordine di arrivo.
 */
final class Connection {

    private static final System.Logger LOG = System.getLogger(Connection.class.getName());

    private final SocketChannel channel;
    private final SelectionKey key;
    private final Session session;
    private final Executor workers;
    private final FrameReader reader = new FrameReader();
    private final Queue<ByteBuffer> outgoing = new ConcurrentLinkedQueue<>();
    private final Queue<byte[]> pending = new ArrayDeque<>();
    private boolean processing;
    private volatile boolean closed;

    Connection(SocketChannel channel, SelectionKey key, Session session, Executor workers) {
        this.channel = channel;
        this.key = key;
        this.session = session;
        this.workers = workers;
    }

    /** Legge tutti i messaggi disponibili e li accoda per l'elaborazione. */
    void onReadable() throws IOException {
        byte[] frame;
        while ((frame = reader.read(channel)) != null) {
            enqueue(frame);
        }
    }

    /** Scrive le risposte pronte; quando non ce ne sono più smette di osservare la scrivibilità. */
    void onWritable() throws IOException {
        ByteBuffer buffer;
        while ((buffer = outgoing.peek()) != null) {
            channel.write(buffer);
            if (buffer.hasRemaining()) {
                return;
            }
            outgoing.poll();
        }
        key.interestOps(SelectionKey.OP_READ);
        // un worker può aver accodato una risposta dopo l'ultimo controllo
        if (!outgoing.isEmpty()) {
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    void close() {
        if (closed) {
            return;
        }
        closed = true;
        key.cancel();
        try {
            channel.close();
        } catch (IOException e) {
            LOG.log(System.Logger.Level.DEBUG, "Errore in chiusura del canale", e);
        }
        session.close();
    }

    private void enqueue(byte[] frame) {
        synchronized (pending) {
            pending.add(frame);
            if (processing) {
                return;
            }
            processing = true;
        }
        try {
            workers.execute(this::drain);
        } catch (RejectedExecutionException e) {
            close();
        }
    }

    private void drain() {
        while (true) {
            byte[] frame;
            synchronized (pending) {
                frame = pending.poll();
                if (frame == null) {
                    processing = false;
                    return;
                }
            }
            send(process(frame));
        }
    }

    private Message process(byte[] frame) {
        try {
            return session.handle(Wire.decode(frame));
        } catch (IOException e) {
            return new Failure("[ERRORE] Messaggio non valido");
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "Errore nell'elaborazione della richiesta", e);
            return new Failure("[ERRORE] Errore interno del server");
        }
    }

    private void send(Message response) {
        if (closed) {
            return;
        }
        try {
            outgoing.add(ByteBuffer.wrap(Wire.encode(response)));
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            key.selector().wakeup();
        } catch (IOException e) {
            LOG.log(System.Logger.Level.ERROR, "Errore nella serializzazione della risposta", e);
        } catch (CancelledKeyException e) {
            // connessione chiusa nel frattempo
        }
    }
}
