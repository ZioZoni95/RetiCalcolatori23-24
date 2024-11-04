package Handlers;

import model.Hotel;
import server.HotelierServer_NIO_TCP;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.StringTokenizer;

public class ReadDataChannelHandler implements Runnable{
    private final SelectionKey key;
    private final HotelierServer_NIO_TCP server;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final int MAX_MESSAGE_LENGTH = 4096; // Limite massimo di lunghezza del messaggio


    public ReadDataChannelHandler(SelectionKey key, HotelierServer_NIO_TCP server){
        this.key = key;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = (ByteBuffer) key.attachment();

            // Fase 1: Lettura della lunghezza del messaggio
            if (buffer == null || buffer.capacity() == Integer.BYTES) {
                if (buffer == null) {
                    buffer = ByteBuffer.allocate(Integer.BYTES);
                    key.attach(buffer);
                }
                clientChannel.read(buffer);

                if (buffer.hasRemaining()) return; // Attende finché la lunghezza non è completamente letta

                buffer.flip();
                int messageLength = buffer.getInt();

                if (messageLength <= 0 || messageLength > MAX_MESSAGE_LENGTH) {
                    System.err.println("Errore: lunghezza del messaggio non valida (" + messageLength + ").");
                    key.cancel();
                    clientChannel.close();
                    return;
                }

                buffer = ByteBuffer.allocate(messageLength);
                key.attach(buffer);
                return;
            }

            // Fase 2: Lettura del contenuto del messaggio
            clientChannel.read(buffer);
            if (buffer.hasRemaining()) return;

            buffer.flip();
            String jsonCommand = new String(buffer.array(), 0, buffer.limit()).trim();
            buffer.clear(); // Pulizia del buffer per l'uso successivo

            // Elabora il comando ricevuto
            String commandLine = mapper.readValue(jsonCommand, String.class);
            System.out.println("Server: comando ricevuto: " + commandLine);

            StringTokenizer token = new StringTokenizer(commandLine);
            String command = token.hasMoreTokens() ? token.nextToken() : "";
            String response;

            if (command.equalsIgnoreCase("searchAllHotels") && token.hasMoreTokens()) {
                String city = token.nextToken();
                List<Hotel> hotels = server.searchAllHotels(city);
                response = hotels.isEmpty() ? "Nessun hotel trovato in " + city : mapper.writeValueAsString(hotels);

            } else if (command.equalsIgnoreCase("searchHotel") && token.countTokens() >= 2) {
                StringBuilder hotelNameBuilder = new StringBuilder(); // costruisce il nome dell'hotel dai token
                while(token.countTokens() > 1){
                    hotelNameBuilder.append(token.nextToken());
                    if(token.countTokens() > 1){
                        hotelNameBuilder.append(" ");
                    }
                }
                String hotelName = hotelNameBuilder.toString().trim();
                String city = token.nextToken();

                Hotel hotel = server.searchHotel(hotelName, city);
                response = (hotel != null) ? mapper.writeValueAsString(hotel) : "Hotel '" + hotelName + "' non trovato in " + city;

            } else {
                response = "Comando non riconosciuto o parametri mancanti.";
            }

            // Serializza la risposta e crea un nuovo buffer per inviarla
            String jsonResponse = mapper.writeValueAsString(response);
            byte[] jsonResponseBytes = jsonResponse.getBytes();

            ByteBuffer responseBuffer = ByteBuffer.allocate(Integer.BYTES + jsonResponseBytes.length);
            responseBuffer.putInt(jsonResponseBytes.length); // Prima parte: lunghezza del messaggio
            responseBuffer.put(jsonResponseBytes);           // Seconda parte: contenuto del messaggio
            responseBuffer.flip();

            // Registra la chiave per scrivere la risposta al client
            server.updateKey(key, SelectionKey.OP_WRITE, responseBuffer);
            System.out.println("Server: risposta preparata per il comando '" + commandLine + "'");
        } catch (IOException e) {
            System.err.println("Read error: " + e.getMessage());
            try {
                key.channel().close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
