package hotelier.server;

import hotelier.protocol.Wire;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/** Ricostruisce, da letture non bloccanti, i messaggi con prefisso di lunghezza (vedi {@link Wire}). */
final class FrameReader {

    private final ByteBuffer header = ByteBuffer.allocate(Integer.BYTES);
    private ByteBuffer payload;

    /** Restituisce il payload di un messaggio completo, oppure null se non sono ancora arrivati tutti i byte. */
    byte[] read(ReadableByteChannel channel) throws IOException {
        if (payload == null) {
            if (channel.read(header) < 0) {
                throw new EOFException();
            }
            if (header.hasRemaining()) {
                return null;
            }
            header.flip();
            int size = header.getInt();
            header.clear();
            if (size < 0 || size > Wire.MAX_FRAME_SIZE) {
                throw new IOException("Messaggio troppo grande: " + size + " byte");
            }
            payload = ByteBuffer.allocate(size);
        }
        if (channel.read(payload) < 0) {
            throw new EOFException();
        }
        if (payload.hasRemaining()) {
            return null;
        }
        byte[] frame = payload.array();
        payload = null;
        return frame;
    }
}
