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
            }else{
                clientChannel.register(key.selector(), SelectionKey.OP_WRITE, responseBuffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
