package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ClientMulticast implements Runnable {
    private MulticastSocket socket; // Socket per la comunicazione multicast
    private InetAddress group_addr; // Indirizzo del gruppo multicast
    private boolean isJoined = false; // Stato di adesione al gruppo multicast

    // Costruttore: inizializza la socket multicast e l'indirizzo del gruppo
    public ClientMulticast(String mcAddress, int mc_port){
        try {
            // Crea una nuova socket multicast con la porta passata come parametro
            socket = new MulticastSocket(mc_port);
            // Ottiene l'indirizzo del gruppo multicast dall'indirizzo passato come parametro
            group_addr = InetAddress.getByName(mcAddress);
        } catch (IOException e) {
            // Gestisce eventuali eccezioni di I/O durante la creazione della socket e dell'indirizzo
            e.printStackTrace();
        }

        // Avvia il thread per la gestione delle notifiche multicast
        Thread thread = new Thread(this);
        thread.start();
    }

    // Effettua la join al gruppo multicast
    public void joinGroup() {
        try {
            // Si unisce al gruppo multicast per ricevere i pacchetti
            socket.joinGroup(group_addr); // Join the multicast group
        } catch (IOException e) {
            // Gestisce eventuali eccezioni di I/O durante l'adesione al gruppo
            e.printStackTrace();
        }
    }

    // Effettua il leave dal gruppo multicast
    public void leaveGroup() {
        try {
            // Se il client è già unito al gruppo, lo lascia
            if (isJoined) {
                socket.leaveGroup(group_addr); // Leave the multicast group
                isJoined = false; // Segna come non unito
            }
        } catch (IOException e) {
            // Gestisce eventuali eccezioni di I/O durante l'uscita dal gruppo
            e.printStackTrace();
        }
    }

    // Metodo eseguito nel thread per la ricezione dei pacchetti multicast
    @Override
    public void run() {

        try {
            // Alloca un array di byte di 1024 byte per ricevere i pacchetti UDP
            byte[] byteArray = new byte[1024];
            // Crea un DatagramPacket con il byte array per ricevere le notifiche
            DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length);

            // Loop che continua finché il thread non viene interrotto
            while (!Thread.interrupted()) {

                // Attende il ricevimento di un pacchetto multicast
                socket.receive(packet);

                // Una volta ricevuto il pacchetto, converte i byte in una stringa di risposta
                String response = new String(packet.getData(), 0, packet.getLength());
                // Stampa la risposta ricevuta sulla console
                System.out.println("\n<Notifica Ricevuta:> " + response + "\n");
                // Esegue il flush di System.out per assicurarsi che la stampa sia visibile
                System.out.flush();
            }
        } catch (IOException e) {
            // Gestisce le eccezioni durante la ricezione dei pacchetti e chiude la socket
            close();
        }
    }

    // Chiude la socket multicast se non è già chiusa
    public void close() {

        if (!socket.isClosed()) {
            // Lascia il gruppo e chiude la socket
            leaveGroup();
            socket.close();
        }
    }
}
