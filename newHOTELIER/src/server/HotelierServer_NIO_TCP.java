package server;
import model.Utente;
import server.networkPackages.RegisterPacket;
import server.networkPackages.RegisterResponsePacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


public class HotelierServer_NIO_TCP implements Runnable {
    //aggiunta 13/10/2024
    private final String serverAddress;
    //----
    private final int PORT;
    private ExecutorService threadpool;
    // private List<Utente> users;

    /**
     * test
     */
    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    public HotelierServer_NIO_TCP(/*aggiunti*/String serverAddress, int PORT) {
        //aggiunta 13/10/2024
        this.serverAddress = serverAddress;
        this.PORT = PORT;
        //----
        //inizializzo il threadpool
        threadpool = Executors.newCachedThreadPool(); //al post di new fixedthreadpool
        //carica utenti da json
        //aggiunta 
    }

    public void run() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            InetSocketAddress address = new InetSocketAddress(serverAddress,PORT);
            serverSocketChannel.socket().bind(address);
            serverSocketChannel.configureBlocking(false);

            //configuro selector per NIO
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server TCP-NIO avviato su " + serverAddress + ":" + PORT);

            //Ciclo principale del server pe accettare e gestire la connessioni

            while (true) {
                selector.select(); //blocco fino a quando c'è un evento
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        //accatta connessione
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println("Connessione accettada da: " + socketChannel.getRemoteAddress());
                    } else if (key.isReadable()) {
                        // Gestisce la lettura dei dati
                        handleClient(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(SelectionKey key) {
        threadpool.submit(() -> {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            try {
                buffer.clear();

                //legge i dati del client
                int bytesRead = socketChannel.read(buffer); // legge i dati dal client
                if (bytesRead == -1) {
                    System.out.println("Client disconnesso: " + socketChannel.getRemoteAddress());
                    socketChannel.close();
                    return;
                }
                if(bytesRead > 0) {
                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    String request = new String(data).trim();
                    System.out.println(" Richiesta ricevuta dal client: " + request);

                    //Elaborazione della richiesta(register)
                    RegisterPacket registerPacket = RegisterPacket.fromJson(request);
                    String responseMessage = register(registerPacket.getUsername(), registerPacket.getPassword());

                    //invia la risposta al client
                    RegisterResponsePacket response = new RegisterResponsePacket(true, responseMessage);
                    ByteBuffer responseBuffer = ByteBuffer.wrap(response.toJson().getBytes());
                    while(responseBuffer.hasRemaining()){
                        socketChannel.write(responseBuffer);
                    }
                    System.out.println("Risposta inviata completamente al client.");
                }
            }catch (java.net.SocketException e) {
                System.out.println("Errore: Connessione resettata dal client.");
                try {
                    socketChannel.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }catch (Exception e) {
                System.out.println("Errore nella gestione del client");
                e.printStackTrace();
                try {
                    socketChannel.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }



    // Simulazione della registrazione utente (per test)
    private synchronized String register(String username, String password) {
        // Logica fittizia di registrazione
        return "Registrazione avvenuta con successo per " + username;
    }

    /*
    main test
     */
    public static void main(String[] args) {
        HotelierServer_NIO_TCP server = new HotelierServer_NIO_TCP("localhost", 8080);
        new Thread(server).start();
    }
}
