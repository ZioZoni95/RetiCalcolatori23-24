package hotelier.server;

import hotelier.model.Hotel;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

/** Invia via UDP multicast la notifica di cambio del primo hotel di una città. */
public final class MulticastNotifier implements Closeable {

    private static final System.Logger LOG = System.getLogger(MulticastNotifier.class.getName());

    private final MulticastSocket socket;
    private final InetAddress group;
    private final int port;

    public MulticastNotifier(String address, int port) throws IOException {
        this.socket = new MulticastSocket();
        this.group = InetAddress.getByName(address);
        this.port = port;
    }

    public void notifyFirstPlace(Hotel hotel) {
        byte[] data = ("Prima posizione cambiata per la città " + hotel.city() + " : " + hotel.name())
                .getBytes(StandardCharsets.UTF_8);
        try {
            socket.send(new DatagramPacket(data, data.length, group, port));
        } catch (IOException e) {
            LOG.log(System.Logger.Level.WARNING, "Invio notifica multicast fallito", e);
        }
    }

    @Override
    public void close() {
        socket.close();
    }
}
