package Handlers;

import model.Hotel;
import server.HotelierServer_NIO_TCP;

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
}
