package hotelier.client;

import hotelier.protocol.Message;
import hotelier.protocol.Wire;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Connessione TCP al server: una richiesta alla volta, con la relativa risposta. */
final class TcpClient implements Closeable {

    private static final int CONNECT_TIMEOUT_MS = 5_000;

    private final Socket socket = new Socket();
    private final InputStream in;
    private final OutputStream out;

    TcpClient(String host, int port) throws IOException {
        socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
        in = new BufferedInputStream(socket.getInputStream());
        out = socket.getOutputStream();
    }

    synchronized Message request(Message request) throws IOException {
        Wire.write(out, request);
        return Wire.read(in);
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            // in chiusura
        }
    }
}
