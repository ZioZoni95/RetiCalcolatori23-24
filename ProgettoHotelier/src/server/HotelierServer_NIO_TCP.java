package server;

import Handlers.HandlerMessages;
import Handlers.ReadDataChannelHandler;
import Handlers.WriteDataChannelHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.Hotel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * HotelierServer_NIO_TCP si occupa della gestione delle richieste TCP
 * utilizzando NIO con multiplexing
 * dei canali mantenendo una
 * connessione di tipo persistente.
 */
public class HotelierServer_NIO_TCP implements Runnable{
    private final int nioTCPport; // porta su cui il server è in listening
    private final String serverAddress;
  //  private Selector selector;
  //  private  volatile boolean running = true;
  //  public final String EXIT_CMD = "exit";
  //  private final String ADD_ANSWER = "echoed by server";
    private final String jsonHotelPathFIle = "resources/Hotels.json";
    private List<Hotel> hotels;
    private ExecutorService threadpool;
   // private final Set<SelectionKey> keysInProgress = ConcurrentHashMap.newKeySet();


    /**
     * Costruttore del ServerNIO
     **/
    public HotelierServer_NIO_TCP(String serverAddress,int nioTCPport) {
        this.nioTCPport = nioTCPport;
        this.serverAddress = serverAddress;
        //loadHotels();
        //printHotels();
        //inizializzo il threadpool
        this.threadpool = Executors.newCachedThreadPool(); //al post di new fixedthreadpool
        // Thread thread = new Thread(this);
        // thread.start();
    }

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

    /**
     *override del moetodo run per avviare il server
     */
/*mio run
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
                    processKey(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private synchronized void processKey(SelectionKey key) {
        if (!key.isValid() || keysInProgress.contains(key)) return;

        keysInProgress.add(key);
        try {
            if (key.isAcceptable()) {
                accettaConnessione(key);
            } else if (key.isReadable()) {
                threadpool.execute(new ReadDataChannelHandler(key, this));
            } else if (key.isWritable()) {
                threadpool.execute(new WriteDataChannelHandler(key, this));
            }
        } finally {
            // Move this removal outside the specific key condition
            // to ensure it always gets removed after processing
            keysInProgress.remove(key);
        }
    }

    public void updateKey(SelectionKey key, int ops, ByteBuffer attachment) {
        key.interestOps(ops);
        key.attach(attachment);
        selector.wakeup();
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
    }*/

    @Override
    public void run(){
        try{
            InetSocketAddress serverSocketAddress = new InetSocketAddress(serverAddress,nioTCPport);
            //apro una una socket channel e la setto non-blocking
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false); //non-blocking
            //apro il selector
            Selector serverSelector = Selector.open();
            //registro la socket channel sul selettore
            serverSocketChannel.register(serverSelector,SelectionKey.OP_ACCEPT);

            while(!Thread.interrupted()){
                serverSelector.select();
                Set<SelectionKey> keys = serverSelector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();

                while(iter.hasNext()){
                    SelectionKey keySelected = iter.next();

                    //se la chiave è accettabilile (ovvero è predisposta all'accept arriva una nuova connessione
                    if(keySelected.isAcceptable()){
                        SocketChannel client = serverSocketChannel.accept();
                        client.configureBlocking(false);

                        //registro il client syul selector per read/write
                        SelectionKey keyOfClient = client.register(serverSelector,SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                        //allego l'attachment
                        keyOfClient.attach(new ReadDataChannelHandler(client));
                    }

                    if(keySelected.isValid() && keySelected.isReadable()){
                        ReadDataChannelHandler clientHandler = (ReadDataChannelHandler) keySelected.attachment();
                        HandlerMessages getMessage = clientHandler.
                    }
                }
            }
        }
    }


    private void accettaConnessione(SelectionKey key) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverSocketChannel.accept();
            clientChannel.configureBlocking(false);
            System.out.println("Connection accepted from " + clientChannel);
            clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(Integer.BYTES));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

