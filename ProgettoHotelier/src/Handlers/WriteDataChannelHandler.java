package Handlers;


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
    public void run() {
        try {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer responseBuffer = (ByteBuffer) key.attachment();

            // Scrive la risposta al client
            clientChannel.write(responseBuffer);

            // Se tutti i dati sono stati inviati, resetta per la lettura successiva
            if (!responseBuffer.hasRemaining()) {
                responseBuffer.clear();
                ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                key.attach(lengthBuffer); //attach del nuovo buffer per leggere la lunghezza

                //registra l'interesse del client in OP_READ
                clientChannel.register(key.selector(), SelectionKey.OP_READ, lengthBuffer);
                System.out.println("Response sent. Ready to read next request.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/*
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
    }*/
}
