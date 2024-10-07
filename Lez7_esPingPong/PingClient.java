package Lez7_esPingPong;

import java.io.IOException;
import java.net.*;

/**
 * PingCLient modella il client del servizio di "Ping Pong"
 */

public class PingClient {
    private final int Buffer_Size = 4;
    private final String PING = "PING";
    private final byte[] pingBuffer;

    /**
     * porta su cui è in ascolto il server
     */
    private final int serverPort;

    /**
     * hostname del server
     */
    private final String serverName;

    /**
     * attesa per la ricezione di una risposta da parte del server
     */
    private final int timeout;

    /**
     *
     * @param serverPort porta del Server
     * @param serverName nome del server
     * @param timeout tempo di attesa per la ricezione di una risposta
     */
    public PingClient(int serverPort,String serverName, int timeout){
        this.serverPort = serverPort;
        this.serverName = serverName;
        this.timeout = timeout;
        this.pingBuffer = PING.getBytes();
    }

    /**
     * avvia il client
     */
    public void start (){
        try(DatagramSocket client = new DatagramSocket()){
            DatagramPacket packetToSend = new DatagramPacket(pingBuffer,pingBuffer.length,
                                                            InetAddress.getByName(this.serverName),
                                                            serverPort);
            System.out.printf("Invio %s al server\n", new String(pingBuffer));
            client.send(packetToSend);;
            //set timeout
            client.setSoTimeout(timeout);

            byte[] buffer = new byte[Buffer_Size];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

            //waiting for server answer
            client.receive(receivedPacket);
            System.out.printf("client received %S\n", new String(receivedPacket.getData()));
       }catch (SocketTimeoutException e) {
            System.out.printf("timeout exception: client waited without answer for  %d ms\n", this.timeout);
        }catch (BindException e){
            System.out.println("Port Busy");
        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
