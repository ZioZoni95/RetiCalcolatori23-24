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
    /*
   @Override
   public void run() {
       try {
           SocketChannel clientChannel = (SocketChannel) key.channel();
           ByteBuffer buffer = (ByteBuffer) key.attachment();

           // Se `buffer` è vuoto, prepariamolo per leggere la lunghezza del messaggio
           if (buffer.position() < Integer.BYTES) {
               clientChannel.read(buffer);
               if (buffer.position() < Integer.BYTES) return; // Aspetta finché la lunghezza non è completamente letta
               buffer.flip();
               int messageLength = buffer.getInt(); //recupero la lunghezza
               buffer.clear();
               buffer.limit(messageLength); //imposto il limite
               return;
           }
           //legge il contenuto
           clientChannel.read(buffer);
           if (buffer.hasRemaining()) return;  // Attende finché il comando non è completamente letto

           buffer.flip();
           String jsonCommand = new String(buffer.array(), 0, buffer.limit()).trim();
           ObjectMapper mapper = new ObjectMapper();

           // Deserializza il comando JSON
           String commandLine = mapper.readValue(jsonCommand, String.class);
           System.out.println("Server: comando ricevuto: " + commandLine);

           // Elabora il comando
           String[] parts = commandLine.split(" ");
           String response;

           if (parts[0].equalsIgnoreCase("searchAllHotels") && parts.length == 2) {
               // Gestione comando searchAllHotels
               String city = parts[1];
               List<Hotel> hotels = server.searchAllHotels(city);
               response = hotels.isEmpty() ? "Nessun hotel trovato in " + city : mapper.writeValueAsString(hotels);

           } else if (parts[0].equalsIgnoreCase("searchHotel") && parts.length == 3) {
               // Gestione comando searchHotel
               String hotelName = parts[1];
               String city = parts[2];
               Hotel hotel = server.searchHotel(hotelName, city);
               response = (hotel != null) ? mapper.writeValueAsString(hotel) : "Hotel '" + hotelName + "' non trovato in " + city;

           } else {
               response = "Comando non riconosciuto o parametri mancanti.";
           }

           // Serializza e prepara la risposta da inviare al client
           String jsonResponse = mapper.writeValueAsString(response);
           byte[] jsonResponseBytes = jsonResponse.getBytes();
           buffer.putInt(jsonResponseBytes.length);
           buffer.put(jsonResponseBytes);
           buffer.flip();

           // Registra la chiave per la scrittura con la risposta
           System.out.println("Registering OP_WRITE for clientChannel");
           clientChannel.register(key.selector(), SelectionKey.OP_WRITE, buffer);
           key.selector().wakeup();
           System.out.println("Server: risposta inviata per il comando '" + commandLine + "'");
       } catch (IOException e) {
           e.printStackTrace();
       }
   }*/
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
            //clientChannel.register(key.selector(), SelectionKey.OP_WRITE, responseBuffer);
            //key.selector().wakeup();
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
