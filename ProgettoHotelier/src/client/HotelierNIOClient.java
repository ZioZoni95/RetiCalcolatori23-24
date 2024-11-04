package client;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class HotelierNIOClient implements Runnable {
    /**
     * dimensione del buffer utilizzato per la lettura
     */
   // private final int BUFFER_DIM = 1024;

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


    @Override
    public void run() {
        try (SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", nioPort))) {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            ObjectMapper mapper = new ObjectMapper();

            System.out.println("Client: connesso al server su porta " + nioPort);
            System.out.println("Benvenuto su Hotelier! Comandi disponibili: " +
                                 "register <username> <password>, " +
                                "login <username> <password>, logout <username>," +
                                "searchHotel <NomeHotel> <Città>, searchAllHotels <Città>, exit");

            while (!this.exit) {
                System.out.print("Inserisci comando: ");
                String commandLine = consoleReader.readLine().trim();

                // Se il comando è "exit", chiudi il client
                if (commandLine.equalsIgnoreCase(EXIT_CMD)) {
                    this.exit = true;
                    System.out.println("Client: chiusura connessione...");
                    continue;
                }

                // Converti il comando in JSON
                String jsonCommand = mapper.writeValueAsString(commandLine);
                System.out.println("Client: invio comando JSON: " + jsonCommand);

                // Invia la lunghezza del comando
                ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                lengthBuffer.putInt(jsonCommand.length());
                lengthBuffer.flip();
                client.write(lengthBuffer);
                lengthBuffer.clear();

                // Invia il comando JSON
                ByteBuffer commandBuffer = ByteBuffer.wrap(jsonCommand.getBytes());
                while(commandBuffer.hasRemaining()){
                    client.write(commandBuffer);
                }

                // Ricezione della lunghezza della risposta
                ByteBuffer responseLengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                client.read(responseLengthBuffer);
                responseLengthBuffer.flip();
                int responseLength = responseLengthBuffer.getInt();
                responseLengthBuffer.clear();

                // Ricezione del contenuto della risposta
                ByteBuffer responseBuffer = ByteBuffer.allocate(responseLength);
                while(responseBuffer.hasRemaining()){
                 client.read(responseBuffer);
                }
                responseBuffer.flip();
                String replyJson = new String(responseBuffer.array(), 0, responseBuffer.limit()).trim();

                // Deserializza e stampa la risposta
                String response = mapper.readValue(replyJson, String.class);
                System.out.printf("Client: risposta dal server - %s\n", response);
                responseBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
