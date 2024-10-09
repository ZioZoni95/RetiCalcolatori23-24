package Assignment7_UDPPing;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;

/**
 * myPingServer modella il server del servizio ping
 */
public class myPingServer {
    /**
     * dimensione del buffer
     */
    private final int BUFEER_SIZE = 100;

    /**
     * Porta su cui è in ascolto il server
     */
    private final int port;

    /**
     * hostname del server
     */
    private final String serverName;

    /**
     *
     * @param port porta su cui è in ascolto il server
     * @param serverName hostname del server
     */

    //costruttore della classe
    public myPingServer(int port, String serverName){
        this.port = port;
        this.serverName = serverName;
    }

    /**
     * avvio il server
     */
    public void start(){
        Random rand = new Random();

        try(DatagramSocket serverSocket = new DatagramSocket(port)){
            byte[] buffer = new byte[BUFEER_SIZE];
            DatagramPacket receivedPacket = new DatagramPacket(buffer,buffer.length);

            while(true){
                serverSocket.receive(receivedPacket);
                //messaggio di ping del client
                String msg = new String(receivedPacket.getData());

                System.out.printf("%s:%d %s ACTION: ",receivedPacket.getAddress(),
                                  receivedPacket.getPort(),msg);

                //simula la latenza di rete
                int delay = 50 * (rand.nextInt(20) + 1);

                if(rand.nextInt(100) > 25){
                    //crea l'echo message
                    DatagramPacket packetToSend = new DatagramPacket(msg.getBytes(),msg.length(),
                                                                     receivedPacket.getSocketAddress());
                    //Latenza rete
                    Thread.sleep(delay);

                    serverSocket.send(packetToSend);

                    System.out.printf("delayed %d ms\n",delay);
                }
                else{
                    System.out.printf("Not sent\n");
                }
            }
        }
        catch (BindException e){
            System.out.println("Port Busy");
        }
        catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }
}
