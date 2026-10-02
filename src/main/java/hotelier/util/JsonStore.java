package hotelier.util;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Lettura e scrittura di file JSON. La scrittura è atomica: un crash non lascia file a metà. */
public final class JsonStore {

    private JsonStore() {
    }

    public static void write(Path file, Object value) throws IOException {
        Path absolute = file.toAbsolutePath();
        Files.createDirectories(absolute.getParent());
        Path tmp = Files.createTempFile(absolute.getParent(), absolute.getFileName().toString(), ".tmp");
        try {
            Json.MAPPER.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), value);
            try {
                Files.move(tmp, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    public static <T> T read(Path file, Class<T> type) throws IOException {
        return Json.MAPPER.readValue(file.toFile(), type);
    }

    /** Legge il file se esiste, altrimenti lo crea con il valore di default e lo restituisce. */
    public static <T> T readOrCreate(Path file, Class<T> type, T defaults) throws IOException {
        if (Files.exists(file)) {
            return read(file, type);
        }
        write(file, defaults);
        return defaults;
    }
}
