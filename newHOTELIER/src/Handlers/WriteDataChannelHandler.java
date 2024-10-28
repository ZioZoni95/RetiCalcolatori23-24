package Handlers;

import server.HotelierServer_NIO_TCP;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class WriteDataChannelHandler implements Runnable{
    private final SelectionKey key; //Chiave di selezione associata al canale pronto per la scrittura

    public WriteDataChannelHandler(SelectionKey key){
        this.key = key;
    }

    @Override
    public void run(){
        try{
            SocketChannel client_channel = (SocketChannel) key.channel();
            String response = (String) key.attachment(); // Recupera la risposta da inviare
            ByteBuffer buff_Response = ByteBuffer.wrap(response.getBytes()); // converte la risposya in un buffer
            client_channel.write(buff_Response); //Scrive la risposta sul canale
            System.out.println("Server: " + response + " inviato al client " + client_channel.getRemoteAddress());

            if(!buff_Response.hasRemaining()){
                buff_Response.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
