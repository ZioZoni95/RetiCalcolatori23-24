package Handlers;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import server.HotelierServer_NIO_TCP;

public class WriteDataChannelHandler implements Runnable{
    private final SelectionKey key; //Chiave di selezione associata al canale pronto per la scrittura
    private final HotelierServer_NIO_TCP server;

    public WriteDataChannelHandler(SelectionKey key,HotelierServer_NIO_TCP server){
        this.key = key;
        this.server=server;
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
                server.updateKey(key, SelectionKey.OP_READ, lengthBuffer);
                System.out.println("Response sent. Ready for the next request.");
            } else {
                server.updateKey(key, SelectionKey.OP_WRITE, responseBuffer);
            }
        } catch (IOException e) {
            //pezzo modificato
            System.err.println("Write error: " + e.getMessage());
            try {
                key.channel().close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
