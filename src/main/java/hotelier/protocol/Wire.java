package hotelier.protocol;

import hotelier.util.Json;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Framing dei messaggi su TCP: 4 byte (big endian) con la lunghezza del payload,
 * seguiti dal payload JSON in UTF-8.
 */
public final class Wire {

    /** Dimensione massima accettata per un messaggio. */
    public static final int MAX_FRAME_SIZE = 1 << 20;

    private Wire() {
    }

    /** Header + payload, pronto da scrivere sul socket. */
    public static byte[] encode(Message message) throws IOException {
        byte[] payload = Json.MAPPER.writerFor(Message.class).writeValueAsBytes(message);
        return ByteBuffer.allocate(Integer.BYTES + payload.length).putInt(payload.length).put(payload).array();
    }

    /** Decodifica il solo payload (senza header). */
    public static Message decode(byte[] payload) throws IOException {
        return Json.MAPPER.readValue(payload, Message.class);
    }

    public static void write(OutputStream out, Message message) throws IOException {
        out.write(encode(message));
        out.flush();
    }

    /** Lettura bloccante di un messaggio completo. */
    public static Message read(InputStream in) throws IOException {
        DataInputStream data = new DataInputStream(in);
        int size = data.readInt();
        if (size < 0 || size > MAX_FRAME_SIZE) {
            throw new IOException("Messaggio troppo grande: " + size + " byte");
        }
        byte[] payload = new byte[size];
        data.readFully(payload);
        return decode(payload);
    }
}
