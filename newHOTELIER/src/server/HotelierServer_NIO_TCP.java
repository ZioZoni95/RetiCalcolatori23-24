package server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Hotel;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * HotelierServer_NIO_TCP si occupa della gestione delle richieste TCP utilizzando NIO con multiplexing mantenendo una
 * connessione di tipo persistente
 *
 *
 */
public class HotelierServer_NIO_TCP{
    //private final String serverAddress;
    private final int BUFFER_DIM = 1024;

    private final int nioTCPport;
   // private ExecutorService threadpool; //threadpool per la gestione dei pacchetti inviati/ricevuti
    // private List<Utente> users;

    /**
     * comando utilizzato dal client per comunicare la fine della comunicazione
     */
    private final String EXIT_CMD = "exit";

    /**
     * messaggio di risposta
     */
    private final String ADD_ANSWER = "echoed by server";
    /**
     * Path del file JSON
     */
    private final String jsonHotelPathFIle = "resources/Hotels.json";

    /**
     * Lista degli Hotels caricati dal file JSON
     */
    private List<Hotel> hotels;

    /**
     * Costruttore del ServerNIO
     *
     * //@param serverAddress indirizzo del server
     * @param nioTCPport    porta del server NIO
     */

    public HotelierServer_NIO_TCP(int nioTCPport) {
        //this.serverAddress = serverAddress;
        this.nioTCPport = nioTCPport;
        loadHotels();
        //inizializzo il threadpool
       // threadpool = Executors.newCachedThreadPool(); //al post di new fixedthreadpool
       // Thread thread = new Thread(this);
       // thread.start();
    }

    /*
     *Metodo per caricare gli hotels da un file JSON
     */
    private void loadHotels(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = Paths.get(jsonHotelPathFIle).toFile();
            if (file.exists()) {
                hotels = mapper.readValue(file, new TypeReference<List<Hotel>>() {
                });
                System.out.println("Hotel Caricati dal file JSON");
            } else {
                System.out.println("Hotels JSON file not found, starting with an empty list.");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Metodo per salvare gli hotel in un file JSON
     */
    private void saveHotels() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(Paths.get(jsonHotelPathFIle).toFile(), hotels);
            System.out.println("Hotels saved to JSON file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //@Override
    public void start() {
        ServerSocketChannel serverSocketChannel;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverSocketChannel.socket();
            InetSocketAddress serverAddress = new InetSocketAddress(nioTCPport);
            serverSocket.bind(serverAddress);
            serverSocketChannel.configureBlocking(false);
            //configuro selector per NIO
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // Aggiungi la shutdown hook per salvare gli hotel alla chiusura del server
            Runtime.getRuntime().addShutdownHook(new Thread(this::saveHotels));

            while (!Thread.interrupted()) {
                selector.select();
                //insieme delle chiavi corrispondenti a canali pronti
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                //iteratore dell'insieme definito sopra
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        //Accetta una nuova connessione creando un socketChannel per la comunicazione
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();

                        System.out.println("Connessione accettata da: " + client);
                        client.configureBlocking(false);
                        this.readChannelBuffer(selector,client);

                    }
                    else if(key.isReadable()){
                        this.readClientmessage(selector,key);
                    }
                    else if(key.isWritable()){
                        this.echoAnswer(selector,key);
                    }
                }
            }
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void readChannelBuffer(Selector sel, SocketChannel c_channel) throws IOException{
        //creazione del buffer
        ByteBuffer  lenght = ByteBuffer.allocate(Integer.BYTES);
        ByteBuffer message = ByteBuffer.allocate(BUFFER_DIM);
        ByteBuffer[] bfs = {lenght,message};
        // aggiunge il canale del client al selector con l'operazione OP_READ
        // e aggiunge l'array di bytebuffer [length, message] come attachment
        c_channel.register(sel, SelectionKey.OP_READ, bfs);
    }

    private void readClientmessage(Selector sel, SelectionKey r_key) throws IOException{
        /**
         * accetta una nuova connessione creando un Socket Channel per la comunicazione con il client
         * che la richiede
         */
        SocketChannel c_channel = (SocketChannel) r_key.channel();
        //recupera l'array di bytebuffer (attachment)
        ByteBuffer[] bfs = (ByteBuffer[]) r_key.attachment();
        c_channel.read(bfs);
        if(!bfs[0].hasRemaining()){
            bfs[0].flip();
            int lenght = bfs[0].getInt();

            if(bfs[1].position() == lenght){
                bfs[1].flip();
                String msg = new String(bfs[1].array()).trim();
                System.out.printf("Server received %s\n", msg);
                if(msg.equals(this.EXIT_CMD)){
                    System.out.println("Server: client connection closed " + c_channel.getRemoteAddress());
                    r_key.cancel();
                    c_channel.close();
                }
                else{
                    /**
                     * aggiunge il canale del client al selector con l'operazione OP_WRITE
                     * e aggiunge il msg ricevuto come attachment (aggiungendo la risposta)
                     */
                    c_channel.register(sel,SelectionKey.OP_WRITE, msg + " " + this.ADD_ANSWER);
                }
            }
        }
    }

    /**
     * scrive il buffer sul canale del client
     *
     * @param key chiave di selezione
     * @throws IOException se si verifica un errore di I/O
     */
    private void echoAnswer(Selector sel, SelectionKey key) throws IOException {
        SocketChannel c_channel = (SocketChannel) key.channel();
        String echoAnsw= (String) key.attachment();
        ByteBuffer bbEchoAnsw = ByteBuffer.wrap(echoAnsw.getBytes());
        c_channel.write(bbEchoAnsw);
        System.out.println("Server: " + echoAnsw + " inviato al client " + c_channel.getRemoteAddress());
        if (!bbEchoAnsw.hasRemaining()) {
            bbEchoAnsw.clear();
            this.readChannelBuffer(sel, c_channel);
        }
    }
}
