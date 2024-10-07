package Assignment7_UDPPing;
import Lez7_esPingPong.PingClient;

import java.io.IOException;
import java.net.*;

/**
 * PingCLient modella il client del servizio di ping
 */
public class myPingClient {
    private final int BUFFER_SIZE  = 100;
    private final String PING =" PING ";

    /**
     * porta su cui è in ascolto il server
     */
    private final int serverPort;

    /**
     * hostname del server
     */
    private final String serverName;

    /**
     * timeout di attesa
     */
    private final int timeout;

    private final int N_SEQNO = 10;

    /**
     * Costruttore per client
     * @param serverPort porta del server
     * @param serverName hostname server
     * @param timeout timeout di attesa in ms
     */
    public myPingClient (int serverPort, String serverName, int timeout){
        this.timeout = timeout;
        this.serverName = serverName;
        this.serverPort = serverPort;
   }

    /**
     * avvio il client
     */
    public void start(){
        //# pacchetti trasmessi
        int packetsTransmitted = 0;
        //# pacchetti ricevuti
        int packetReceived = 0;
        //RTT massimo
        long rttmax = 0;
        long rttmin = 1000;
        //somma degli rtt (per calcolo media)
        long rttSum = 0;

        try(DatagramSocket client = new DatagramSocket()){
            for(int seqno = 0; seqno < N_SEQNO; seqno++){
                //timestamp
                long initTime = System.currentTimeMillis();

                //messaggio di ping
                String msg = PING + " " + seqno + " " + initTime;

                byte[] pingBuffer = msg.getBytes();
                DatagramPacket packetToSend = new DatagramPacket(pingBuffer,pingBuffer.length,
                                                                InetAddress.getByName(this.serverName),
                                                                this.serverPort);
                //invio del ping
                client.send(packetToSend);
                packetsTransmitted++;
                //timeout
                client.setSoTimeout(timeout);

                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket receivedPacket = new DatagramPacket(buffer,buffer.length);

                //rimane in  attesa del server
                try{
                    client.receive((receivedPacket));
                    long endTIme = System.currentTimeMillis();

                    //update statictis
                    long RTT = endTIme - initTime;
                    rttmin = Math.min(rttmin,RTT);
                    rttmax = Math.max(rttmax,RTT);
                    rttSum = rttSum + RTT;
                    packetReceived++;
                    System.out.printf("%s RTT: %d ms\n",new String(pingBuffer),RTT);
                }
                catch (SocketTimeoutException e){
                    System.out.printf("%s RTT *\n",msg);
                }
            }
        }
        catch (BindException e){
            System.out.println(" Port is Busy ");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            //stampa le diverse statistiche ottenute

            float percPackLoss = (float) (packetsTransmitted - packetReceived) / (float) packetsTransmitted;
            percPackLoss *= 100;

            float rttAvg = (packetsTransmitted != 0) ? (float) rttSum / (float) packetsTransmitted : 0;

            System.out.println("---- PING STATISTICS ----");
            System.out.printf("%d packets trasmitted, %d packets received, %.0f%% packet loss\n",
                              packetsTransmitted,packetReceived,percPackLoss);
            System.out.printf(" round trip (ms) min/avg/max = %d/%.2f/%d\n",
                              rttmin,rttAvg,rttmax);
        }
    }
}
