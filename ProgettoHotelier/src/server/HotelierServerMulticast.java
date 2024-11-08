package server;

import model.Hotel;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * La classe HotelierServerMulticast è responsabile dell'invio di pacchetti UDP per notificare il cambiamento
 * della prima posizione nel ranking locale degli hotel. Utilizza una socket multicast per inviare i pacchetti
 * a tutti i client loggati che ascoltano su un gruppo multicast specificato.
 */
public class HotelierServerMulticast {

    // Indirizzo del gruppo multicast a cui inviare i pacchetti
    private final String mcAddress;
    // Porta del gruppo multicast a cui inviare i pacchetti
    private final int mc_port;
    // Socket multicast UDP per inviare pacchetti
    private MulticastSocket socket;
    // Indirizzo del gruppo multicast (rappresenta il gruppo di client loggati)
    private InetAddress group;

    /**
     * Costruttore della classe. Inizializza la socket multicast e il gruppo.
     *
     * @param mcAddress Indirizzo del gruppo multicast a cui inviare i pacchetti.
     * @param mc_port Porta del gruppo multicast.
     * @throws IOException Se si verifica un errore durante l'inizializzazione della socket o la risoluzione dell'indirizzo.
     */
    public HotelierServerMulticast(String mcAddress, int mc_port) throws IOException {
        this.mcAddress = mcAddress;  // Imposta l'indirizzo del gruppo multicast
        this.mc_port = mc_port;      // Imposta la porta per il multicast
        socket = new MulticastSocket();  // Crea una nuova socket multicast
        group = InetAddress.getByName(mcAddress);  // Risolve l'indirizzo del gruppo multicast
    }

    /**
     * Invia un pacchetto UDP per notificare il cambiamento nella posizione di un hotel,
     * in particolare il cambiamento della "prima posizione" nel ranking locale.
     *
     * @param hotel L'hotel che ha cambiato posizione e deve essere notificato ai client.
     */
    public void notifyFirstPosition(Hotel hotel) {
        try {
            // Componi il messaggio di notifica includendo il nome dell'hotel e la città
            String response = "Prima posizione cambiata per la città " + hotel.getCity() + " : " + hotel.getName();
            // Converte il messaggio in un array di byte per poterlo inviare tramite UDP
            byte[] responseBytes = response.getBytes();
            // Crea un pacchetto UDP che contiene i dati da inviare, includendo l'indirizzo del gruppo e la porta
            DatagramPacket messagePacket = new DatagramPacket(responseBytes, responseBytes.length, group, mc_port);
            // Invia il pacchetto UDP tramite la socket multicast
            socket.send(messagePacket);

        } catch (IOException e) {
            // Gestisce eventuali errori di invio del pacchetto
            e.printStackTrace();  // Stampa l'errore nel caso di un'eccezione
            close();  // Chiude la socket in caso di errore per evitare perdite di risorse
        }
    }

    /**
     * Chiude la socket multicast se era stata creata e se è ancora aperta.
     * Questo metodo viene invocato per liberare risorse in caso di errore o quando la classe non è più necessaria.
     */
    private void close() {
        // Verifica se la socket è stata creata e non è ancora chiusa
        if (socket != null && !socket.isClosed()) {
            socket.close();  // Chiude la socket multicast
        }
    }
}
