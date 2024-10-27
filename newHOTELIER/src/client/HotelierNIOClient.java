package client;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Hotel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class HotelierNIOClient {
    /**
     * dimensione del buffer utilizzato per la lettura
     */
    private final int BUFFER_DIM = 1024;

    /**
     * comando utilizzato dal client per comunicare la fine della comunicazione
     */
    private final String EXIT_CMD = "exit";

    /**
     *Porta su cui il server è in listening
     */
    private final int nioPort;

    /**
     * @return true se il client è in terminazione
     * false altrimenti
     */
    private boolean exit;

    /**
     * Costruttore del Client
     * @param port porta su cui iil client è in ascolto
     */
    public HotelierNIOClient(int port){
        this.nioPort = port;
        this.exit = false;
    }


    public void start(){
        try(SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost",nioPort));){
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Client: connesso");
            System.out.println("Digita exit per uscire, i messaggi inviati al server: ");

            while(!this.exit){
                String msg = consoleReader.readLine().trim();

                //creo il messaggio da inviare al server
                ByteBuffer lenght = ByteBuffer.allocate(Integer.BYTES);
                lenght.putInt(msg.length());
                lenght.flip();
                client.write(lenght);
                lenght.clear();

                //la seconda parte del messaggio contiene il messaggio da inviare
                ByteBuffer readBuffer = ByteBuffer.wrap(msg.getBytes());

                client.write(readBuffer);
                readBuffer.clear();

                if(msg.equals(this.EXIT_CMD)){
                    this.exit = true;
                    continue;
                }
                ByteBuffer reply = ByteBuffer.allocate(BUFFER_DIM);
                client.read(reply);
                reply.flip();
                System.out.printf("Client: il server ha inviato %s\n", new String(reply.array()).trim());
                reply.clear();
            }
            System.out.println("Client: chiusura");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
