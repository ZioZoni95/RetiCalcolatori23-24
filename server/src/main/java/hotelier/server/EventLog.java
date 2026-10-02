package hotelier.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Registro degli eventi del server (login, recensioni, connessioni...), consultabile dalle interfacce. */
public final class EventLog {

    /** Un evento con il suo istante. */
    public record Entry(LocalDateTime time, String text) {
        private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

        @Override
        public String toString() {
            return "[" + FORMAT.format(time) + "] " + text;
        }
    }

    private static final int CAPACITY = 500;

    private final Deque<Entry> entries = new ArrayDeque<>();
    private final List<Consumer<Entry>> listeners = new CopyOnWriteArrayList<>();

    public void log(String text) {
        Entry entry = new Entry(LocalDateTime.now(), text);
        synchronized (this) {
            if (entries.size() == CAPACITY) {
                entries.removeFirst();
            }
            entries.addLast(entry);
        }
        listeners.forEach(listener -> listener.accept(entry));
    }

    /** Gli ultimi eventi, dal più vecchio al più recente. */
    public synchronized List<Entry> recent() {
        return List.copyOf(entries);
    }

    /** Il listener viene invocato nel thread che registra l'evento. */
    public void addListener(Consumer<Entry> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<Entry> listener) {
        listeners.remove(listener);
    }
}
