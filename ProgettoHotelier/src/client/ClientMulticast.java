package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ClientMulticast implements Runnable {
    private MulticastSocket socket;
    private InetAddress group_addr;
    private boolean isJoined = false;

    public ClientMulticast(String mcAddress, int mc_port){
        try {
            // instazione una nuova socket multicast con porta passata
            socket = new MulticastSocket(mc_port);
            // ottengo address da indirizzo passato
            group_addr = InetAddress.getByName(mcAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // avvio il thread
        Thread thread = new Thread(this);
        thread.start();
    }

    // effettua la join del gruppo multicast
    public void joinGroup() {
        try {
            if (!isJoined) {
                socket.joinGroup(group_addr); // Join the multicast group
                isJoined = true;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    // effettua la leave dal gruppo multicast
    public void leaveGroup() {
        try {
            if (isJoined) {
                socket.leaveGroup(group_addr);
                isJoined = false; // Mark as not joined
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        try {
            // alloco un byte array di 1024 byte
            byte[] byteArray = new byte[1024];
            // instanzio un DatagramPacket avente come array di byte byteArray e lunghezza la lunghezza di byteArray per ricever notifiche Udp
            DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length);

            // itero finchè il thread non viene interrotto
            while (!Thread.interrupted()) {

                // mi metto in attesa della notifica Udp
                socket.receive(packet);

                // notifica ricevuta
                // converto i byte ricevuti in una stringa risposta
                String response = new String(packet.getData(), 0, packet.getLength());
                // stampo la rispota
                System.out.println("<Notifica Ricevuta:> " + response + "\n");
                // eseguo il flush di system out
                System.out.flush();
            }
        } catch (IOException e) {
            close();
        }
    }

    // chiude la socket multicast se ancora aperta
    public void close() {

        if (!socket.isClosed()) {
            leaveGroup();
            socket.close();
        }
    }
}
