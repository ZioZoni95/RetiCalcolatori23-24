package hotelier.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/** Riceve le notifiche multicast del server mentre il client è iscritto al gruppo (cioè dopo il login). */
final class MulticastListener implements Closeable {

    private final MulticastSocket socket;
    private final InetSocketAddress group;
    private final Consumer<String> onNotification;
    private boolean joined;
    private volatile boolean closed;

    MulticastListener(String address, int port, Consumer<String> onNotification) throws IOException {
        this.socket = new MulticastSocket(port);
        this.group = new InetSocketAddress(InetAddress.getByName(address), 0);
        this.onNotification = onNotification;
        Thread thread = new Thread(this::receiveLoop, "multicast-listener");
        thread.setDaemon(true);
        thread.start();
    }

    synchronized void join() throws IOException {
        if (!joined) {
            socket.joinGroup(group, null);
            joined = true;
        }
    }

    synchronized void leave() throws IOException {
        if (joined) {
            socket.leaveGroup(group, null);
            joined = false;
        }
    }

    private void receiveLoop() {
        byte[] buffer = new byte[1024];
        while (!closed) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                return; // socket chiusa
            }
            onNotification.accept(new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8));
        }
    }

    @Override
    public void close() {
        closed = true;
        try {
            leave();
        } catch (IOException e) {
            // in chiusura
        }
        socket.close();
    }
}
