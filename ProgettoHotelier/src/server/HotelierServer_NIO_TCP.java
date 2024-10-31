package server;

import Handlers.ReadDataChannelHandler;
import Handlers.WriteDataChannelHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.Hotel;
import model.Ratings;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * HotelierServer_NIO_TCP si occupa della gestione delle richieste TCP utilizzando NIO con multiplexing mantenendo una
 * connessione di tipo persistente
 *
 *
 */
public class HotelierServer_NIO_TCP implements Runnable{
    private final int nioTCPport; // porta su cui il server è in listening
    private Selector selector;
    private  volatile boolean running = true;
    public final String EXIT_CMD = "exit";
    private final String ADD_ANSWER = "echoed by server";
    private final String jsonHotelPathFIle = "resources/Hotels.json";
    private List<Hotel> hotels;
    private final ExecutorService threadpool;

    /**
     * Costruttore del ServerNIO
     **/
    public HotelierServer_NIO_TCP(int nioTCPport) {
        this.nioTCPport = nioTCPport;
        loadHotels();
        //printHotels();
       //inizializzo il threadpool
       this.threadpool = Executors.newCachedThreadPool(); //al post di new fixedthreadpool
       // Thread thread = new Thread(this);
       // thread.start();
    }
/*
    /**
     * Test code
     *//*
    private void printHotels() {
        if (hotels != null && !hotels.isEmpty()) {
            System.out.println("Lista degli Hotels:");
            for (Hotel hotel : hotels) {
                System.out.println(hotel);
            }
        } else {
            System.out.println("Nessun hotel trovato.");
        }
    }
*/
    /*
     *Metodo per caricare gli hotels da un file JSON
     */
    private void loadHotels(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        /*
         * rende visibile all'ObjectMapper gli attributi privati della classe di cui l'oggetto da serializzare
         * ne l'istanza
         */
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        try(FileChannel fileChannel = FileChannel.open(Paths.get(jsonHotelPathFIle),StandardOpenOption.READ)){
            ByteBuffer buffer = ByteBuffer.allocateDirect((int) fileChannel.size());
            fileChannel.read(buffer);
            buffer.flip(); //read mode buffer

            //crea una stringa dai dati del buffer
            byte[] dataBytes = new byte[buffer.remaining()];
            buffer.get(dataBytes);

            //Legge il contenutoswl buffer come Stringa JSON
            String jsonData = new String(dataBytes, StandardCharsets.UTF_8);

            //deserializza in una lista di hotel

            hotels = mapper.readValue(jsonData, new TypeReference<List<Hotel>>() {});
            System.out.println("Caricamento file Hotels.json: Completato");
        }catch (IOException e){
            System.err.println("Caricamento file Hotels.json: errore");
            e.printStackTrace();
        }
    }

    /**
     * Metodo per salvare gli hotel in un file JSON
     */
    private void saveHotels() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try (FileChannel fileChannel = FileChannel.open(Paths.get(jsonHotelPathFIle),
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            String jsonData = mapper.writeValueAsString(hotels);
            ByteBuffer buffer = ByteBuffer.wrap(jsonData.getBytes());

            while (buffer.hasRemaining()) {
                fileChannel.write(buffer); // Scrive il buffer nel canale
            }
            System.out.println("Hotel salvati nel file JSON.");
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del file JSON.");
            e.printStackTrace();
        }
    }


/*
    //metodo per avviare il server
    public void start() {
        ServerSocketChannel serverSocketChannel;
        try {
            //configura il canale del server per ascoltare le connessioni sulla porta specificata
            serverSocketChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverSocketChannel.socket();
            InetSocketAddress serverAddress = new InetSocketAddress(nioTCPport);
            serverSocket.bind(serverAddress);
            serverSocketChannel.configureBlocking(false); //non-blocking
            //configuro selector per NIO
            Selector selector = Selector.open(); //selettore per il multiplexing dei canali
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); /*registra il canale del server per
                                                                              accettare connessioni*/

            // Aggiungi la shutdown hook per salvare gli hotel alla chiusura del server
          //  Runtime.getRuntime().addShutdownHook(new Thread(this::saveHotels));

    /*        while (!Thread.interrupted()) {
                selector.select(); //blocca fino a quando almeno un canale è pronto
                //insieme delle chiavi corrispondenti a canali pronti
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                //iteratore dell'insieme definito sopra
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove(); //rimuove la chiave per evitare di elaborarla nuovamente
                    if (key.isAcceptable()) {
                        accettaConnessione(selector,key); //gestisce l'accettazione di una connessione
                        /*ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        //SocketChannel client = server.accept();

     /*                   System.out.println("Connessione accettata da: " + client);
                        client.configureBlocking(false);
                        this.readChannelBuffer(selector,client);

                         */
     /*               }
                    else if(key.isReadable()){
                        threadpool.execute(new ReadDataChannelHandler(key,this)); //delega all'handler di lettura
                    }
                    else if(key.isWritable()){
                        threadpool.execute(new WriteDataChannelHandler(key)); // delega all'handler la scrittura;
                    }
                }
            }
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(nioTCPport));
            serverSocketChannel.configureBlocking(false);
            this.selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server avviato sulla porta " + nioTCPport);

            while (running) {
                selector.select(); // Blocca fino a quando un canale è pronto
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isAcceptable()) {
                        accettaConnessione(key); // Gestisce una nuova connessione
                    } if (key.isReadable()) {
                        threadpool.execute(new ReadDataChannelHandler(key, this));
                    }  if (key.isWritable()) {
                        threadpool.execute(new WriteDataChannelHandler(key));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        running = false;
        try {
            selector.close();
            threadpool.shutdown();
            System.out.println("Server chiuso in modo sicuro.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void accettaConnessione(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        System.out.println("Connessione accettata da " + clientChannel);
        clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(Integer.BYTES));
    }


    // Metodo per cercare un hotel per nome e città
    public synchronized Hotel searchHotel(String nomeHotel, String citta) {
        return hotels.stream()
                .filter(hotel -> hotel.getName().equalsIgnoreCase(nomeHotel) && hotel.getCity().equalsIgnoreCase(citta))
                .findFirst()
                .orElse(null);
    }


    // Metodo per cercare tutti gli hotel di una città ordinati per ranking
    public synchronized List<Hotel> searchAllHotels(String citta) {
        return hotels.stream()
                .filter(hotel -> hotel.getCity().equalsIgnoreCase(citta))
             //   .sorted((h1, h2) -> Ratings.compare(h2.getRatings(), h1.getRate()))
                .collect(Collectors.toList());
    }
}

