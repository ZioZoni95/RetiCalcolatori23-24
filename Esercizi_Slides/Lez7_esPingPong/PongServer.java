package Lez7_esPingPong;

import java.io.IOException;
import java.net.*;
import java.util.Random;

/**
 * PongServer modella il server del servizio di "Ping Pong"
 */

public class PongServer {
    private final int BUFF_SYZE = 4;
    private final String REPLY = "PONG";
    private final byte[] replyBuffer;
    /**
     * server listening port
     */
    private final int port;
    /**
     * server hostname
     */
    private final String serverName;

    /**
     *
     * @param port listening port
     * @param serverName hostname server
     */
    public PongServer(int port, String serverName){
        this.port = port;
        this.serverName = serverName;
        this.replyBuffer = REPLY.getBytes();
    }

    /**
     * start server
     */
    public void start (){
        Random rand = new Random();
        try(DatagramSocket serverSocket = new DatagramSocket(port)){
            byte [] buffer = new byte[BUFF_SYZE];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            System.out.println("Server wainting for PING message");

            while(true){
                serverSocket.receive(receivedPacket);
                String msg = new String(receivedPacket.getData());

                System.out.printf("Server reveiced %s\n", msg);

                if (msg.equals("PING") && rand.nextBoolean()) {
                    DatagramPacket packetToSend = new DatagramPacket(replyBuffer, replyBuffer.length,
                                                                    InetAddress.getByName(this.serverName),
                                                                    receivedPacket.getPort());
                    serverSocket.send(packetToSend);
                }
            }
        }
        catch(BindException e){
            System.out.println("Port Busy");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}
