package server;

import HandlerMessages.Request_ResponseMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HotelierNIOServerTCP implements Runnable{
    private final String serverAddress;
    private final int nioTCPPort;
    private ExecutorService threadpool;

    public HotelierNIOServerTCP(String serverAddress, int nioTCPPort){
        this.serverAddress = serverAddress;
        this.nioTCPPort = nioTCPPort;
        threadpool = Executors.newCachedThreadPool();
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            InetSocketAddress serverSocketAddress = new InetSocketAddress(serverAddress, nioTCPPort);

            // Open a ServerSocketChannel and configure it as non-blocking
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);

            // **Bind the server socket channel to the server address and port**
            serverSocketChannel.bind(serverSocketAddress);

            // Open the selector and register the server channel for ACCEPT operations
            Selector serverSelector = Selector.open();
            serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);

            while (!Thread.interrupted()) {
                serverSelector.select();
                Set<SelectionKey> keys = serverSelector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();

                while (iter.hasNext()) {
                    SelectionKey keySelected = iter.next();

                    // Accept new connections
                    if (keySelected.isAcceptable()) {
                        SocketChannel client = serverSocketChannel.accept();
                        client.configureBlocking(false);

                        // Register the client channel with the selector for read/write operations
                        SelectionKey keyOfClient = client.register(serverSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        keyOfClient.attach(new HotelierServerConnectionClientHandler(client));
                    }

                    // Handle readable events
                    if (keySelected.isValid() && keySelected.isReadable()) {
                        HotelierServerConnectionClientHandler clientHandler = (HotelierServerConnectionClientHandler) keySelected.attachment();
                        Request_ResponseMessage packet = clientHandler.handleRead();

                        if (packet != null) {
                            threadpool.submit(() -> {
                                clientHandler.handlePacket(packet);
                            });
                        }

                        if (!clientHandler.isConnected()) {
                            clientHandler.close();
                            keySelected.cancel();
                        }
                    }

                    // Handle writable events
                    if (keySelected.isValid() && keySelected.isWritable()) {
                        HotelierServerConnectionClientHandler clientHandler = (HotelierServerConnectionClientHandler) keySelected.attachment();
                        clientHandler.handleWrite();
                        if (!clientHandler.isConnected()) {
                            clientHandler.close();
                            keySelected.cancel();
                        }
                    }
                    iter.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
