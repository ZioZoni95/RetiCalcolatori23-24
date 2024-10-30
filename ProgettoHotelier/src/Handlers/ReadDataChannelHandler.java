package Handlers;

import model.Hotel;
import server.HotelierServer_NIO_TCP;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public class ReadDataChannelHandler implements Runnable{
    private final SelectionKey key;
    private final HotelierServer_NIO_TCP server;

    public ReadDataChannelHandler(SelectionKey key, HotelierServer_NIO_TCP server){
        this.key = key;
        this.server = server;
    }
/*


    @Override
    public void run(){
        try{
            SocketChannel client_channel = (SocketChannel) key.channel();
            ByteBuffer buffer =(ByteBuffer) key.attachment(); //Recupera il buffer associato alla chiave

            //Se stiamo leggendo la lunghezza del messaggio
            if(buffer.capacity() == Integer.BYTES){
                client_channel.read(buffer); //legge la lunghezza del messaggio
                if(!buffer.hasRemaining()) {
                    buffer.flip();
                    int lunghezza = buffer.getInt(); // ottiengo la lunghezza del messaggio
                    buffer = ByteBuffer.allocate(lunghezza); // Alloco un nuovo buffer per il messaggio
                    client_channel.register(key.selector(), SelectionKey.OP_READ, buffer);
                }
            }else {
                //leggo il messaggio vero e proprio
                client_channel.read(buffer); // leggo il messaggio
                if (!buffer.hasRemaining()){
                    buffer.flip();
                    String msg = new String(buffer.array()).trim(); //Converte il buffer in una stringa
                    System.out.printf("comando ricevuto dal client: %s\n",msg);

                    if(msg.equals((server.EXIT_CMD))){
                        //Chiude la connessione se il client invia il comando di uscita
                        System.out.println("Server : client disconnesso " + client_channel);
                        key.cancel();
                        client_channel.close();
                    }else{
                        //Elabora il comando ricevuto
                        String[] parts = msg.split(" ");
                        String command = parts[0];
                        String response = "";

                        switch (command){
                            case "searchHotel":
                                if(parts.length == 3){
                                    Hotel hotel = server.searchHotel(parts[1],parts[2]);
                                    response = hotel != null ? hotel.toString() : "Hotel non Trovato";
                                    }else {
                                    response = "formato del comando non valido";
                                }
                                break;
                            case "searchAllHotels":
                                if (parts.length == 2){
                                    List<Hotel> hotels = server.searchAllHotels(parts[1]);
                                    response = hotels.isEmpty() ? "Nessun hotel trovato" : hotels.toString();
                                }else {
                                    response = "formato del comando non valido";
                                }
                                break;

                            default:
                                response = "Comando sconosciuto";
                        }
                        client_channel.register(key.selector(),SelectionKey.OP_WRITE, response);
                    }
                }
            }
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    */
   @Override
   public void run() {
       try {
           SocketChannel clientChannel = (SocketChannel) key.channel();
           ByteBuffer lengthBuffer = (ByteBuffer) key.attachment();

           // Leggi la lunghezza del comando
           if (lengthBuffer.remaining() > 0) {
               clientChannel.read(lengthBuffer);
           }
           if (lengthBuffer.hasRemaining()) return;  // Attende finché la lunghezza non è completamente letta

           lengthBuffer.flip();
           int messageLength = lengthBuffer.getInt();

           // Prepara il buffer per il comando vero e proprio
           ByteBuffer commandBuffer = ByteBuffer.allocate(messageLength);
           clientChannel.read(commandBuffer);

           if (commandBuffer.hasRemaining()) return;  // Attende finché il comando non è completamente letto

           commandBuffer.flip();
           String jsonCommand = new String(commandBuffer.array(), 0, commandBuffer.limit()).trim();
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
           ByteBuffer responseBuffer = ByteBuffer.allocate(Integer.BYTES + jsonResponseBytes.length);
           responseBuffer.putInt(jsonResponseBytes.length);
           // ByteBuffer responseBuffer = ByteBuffer.allocate(Integer.BYTES + jsonResponse.length());
           //responseBuffer.putInt(jsonResponse.length());
           System.out.println("fino a qui ci sono");

           //qui esplode
           responseBuffer.put(jsonResponseBytes);
           responseBuffer.flip();

           // Registra la chiave per la scrittura con la risposta
          /* clientChannel.register(key.selector(), SelectionKey.OP_WRITE, responseBuffer);*/
           System.out.println("Registering OP_WRITE for clientChannel");
           clientChannel.register(key.selector(), SelectionKey.OP_WRITE, responseBuffer);
           key.selector().wakeup();
           System.out.println("Server: risposta inviata per il comando '" + commandLine + "'");
           responseBuffer.clear();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }
}
