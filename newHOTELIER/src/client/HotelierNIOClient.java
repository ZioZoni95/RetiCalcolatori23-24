package client;

import client.networkPackages.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class HotelierNIOClient {

    public static void main(String[] args) {
        // Configura l'indirizzo e la porta del server a cui il client si connetterà
        String serverAddress = "localhost";
        int port = 8080;

        try {
            // Crea un SocketChannel non bloccante e connettiti al server
            SocketChannel client = SocketChannel.open(new InetSocketAddress(serverAddress, port));
            client.configureBlocking(true); // Imposta il canale in modalità bloccante per semplicità

            // Crea un pacchetto di registrazione (RegisterPacket) con i dati dell'utente
            RegisterPacket registerPacket = new RegisterPacket("userNIO", "passwordNIO");

            // Serializza il pacchetto in JSON e lo scrive nel buffer
            ByteBuffer buffer = ByteBuffer.wrap(registerPacket.toJson().getBytes());
            client.write(buffer); // Invia i dati al server

            // Pulisci il buffer per ricevere la risposta
            buffer.clear();

            StringBuilder response = new StringBuilder();
            int bytesRead;
            while ((bytesRead = client.read(buffer)) > 0) {  // Continua a leggere fino a quando ci sono dati
                buffer.flip();  // Passa a modalità lettura
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                response.append(new String(data));
                buffer.clear();  // Pulisci il buffer per la prossima lettura
                System.out.println("Risposta dal server NIO: " + response);
            }

            // Verifica se c'è un problema di bytesRead == -1 (client chiuso inaspettatamente)
            if (bytesRead == -1) {
                System.out.println("Il server ha chiuso la connessione.");
            }



            // Chiudi il canale del client
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}