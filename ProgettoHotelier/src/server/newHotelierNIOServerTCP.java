package server;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class newHotelierNIOServerTCP implements Runnable{
    private final String serverAddress;
    private final int nioTCPPort;
    private ExecutorService threadpool;

    public newHotelierNIOServerTCP(String serverAddress,int nioTCPPort){
        this.serverAddress = serverAddress;
        this.nioTCPPort = nioTCPPort;
        threadpool = Executors.newCachedThreadPool();
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run(){
        try{
            InetSocketAddress serverSocketAddress = new InetSocketAddress(serverAddress,nioTCPPort);
            //apro una una socket channel e la setto non-blocking
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false); //non-blocking
            //apro il selector
            Selector serverSelector = Selector.open();
            //registro la socket channel sul selettore
            serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);

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
                        keyOfClient.attach(new )
        }
    }
}
