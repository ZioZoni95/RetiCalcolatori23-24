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

/*
    public void start(){
        try(SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost",nioPort));){
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            ObjectMapper mapper = new ObjectMapper(); //oggetto per gestire la serializzazione

            System.out.println("Client: connesso");
            System.out.println("Benvenuto su Hotelier! Scegli un'opzione : searchHotels searchallHotels ");


            while(!this.exit){
                //legge l'input dalla cli
                String msg = consoleReader.readLine().trim();

                /**
                 * Serializzazione: Converte il messaggio in JSON
                 */
     //           String jsonMsg = mapper.writeValueAsString(msg);

                /**
                 * Invio Lunghezza messaggio: Prima di inviare il messaggio
                 * si calcola e si invia la sua lunghezza. In questo modo il server
                 * sa quanti byte aspettarsi
                 */

                //creo il messaggio da inviare al server
/*                ByteBuffer lenghtBuffer = ByteBuffer.allocate(Integer.BYTES);
                lenghtBuffer.putInt(jsonMsg.length());
                lenghtBuffer.flip(); //passa il buffer in modalità lettura
                client.write(lenghtBuffer);
               // lenght.clear(); //Rimuove i dati dal buffer per riutilizzarlo
                /*



                //la seconda parte del messaggio contiene il messaggio da inviare
                ByteBuffer messageBuffer = ByteBuffer.wrap(msg.getBytes());
                client.write(readBuffer);
                //readBuffer.clear();

                 */
                //invio del messaggio json
      /*          ByteBuffer messageBuffer = ByteBuffer.wrap(jsonMsg.getBytes());
                client.write(messageBuffer);

                if(msg.equals(this.EXIT_CMD)){
                    this.exit = true;
                    continue;
                }
                //Legge la lunghezza del messaggio di risposta
                ByteBuffer reply = ByteBuffer.allocate(Integer.BYTES);
                client.read(reply);
                reply.flip();
                int responseLenght = reply.getInt();

                //reply.clear();

                /*Allocazione di un buffer dinamico per la risposta in base alla dim ricevuta*/
  /*             ByteBuffer replyBuffer = ByteBuffer.allocate(responseLenght);
               client.read(replyBuffer);
               replyBuffer.flip();
               String replyJson = new String(replyBuffer.array(), 0 , replyBuffer.limit()).trim();


               /*//**Lettura della risposta**: Si assicura di leggere tutti i byte del messaggio
               while (replyBuffer.hasRemaining()){
                   client.read(replyBuffer);
               }
              //  System.out.printf("Client: il server ha inviato %s\n", new String(reply.array()).trim());
              */
  /*              String r_msg = mapper.readValue(replyJson,String.class); //conversione json a string
                System.out.printf("Client: il server ha inviato %s\n",r_msg);
                replyBuffer.clear();
            }
            System.out.println("Client: chiusura");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }*/

    @Override
    public void run() {
        try (SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", nioPort))) {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            ObjectMapper mapper = new ObjectMapper();

            System.out.println("Client: connesso al server su porta " + nioPort);
            System.out.println("Benvenuto su Hotelier! Comandi disponibili: searchHotel <NomeHotel> <Città>, searchAllHotels <Città>, exit");

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

                // Invia il comando JSON
                ByteBuffer commandBuffer = ByteBuffer.wrap(jsonCommand.getBytes());
                client.write(commandBuffer);

                // Ricezione della lunghezza della risposta
                ByteBuffer responseLengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                client.read(responseLengthBuffer);
                responseLengthBuffer.flip();
                int responseLength = responseLengthBuffer.getInt();

                // Ricezione del contenuto della risposta
                ByteBuffer responseBuffer = ByteBuffer.allocate(responseLength);
                client.read(responseBuffer);
                responseBuffer.flip();
                String replyJson = new String(responseBuffer.array(), 0, responseBuffer.limit()).trim();

                // Deserializza e stampa la risposta
                String response = mapper.readValue(replyJson, String.class);
                System.out.printf("Client: risposta dal server - %s\n", response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
