package Lez7_slides_srcFiles;
import java.net.*;

public class Sender {
    public static void main(String[] args){
        try(DatagramSocket clientSocket = new DatagramSocket()){
            byte[] buffer = "1234567890abcdefghijklmnopqrstuvwxyz".getBytes("US-ASCII");
            InetAddress address = InetAddress.getByName("127.0.0.1");
            for(int i = 1;i<buffer.length; i++){
                /**
                 *Costruisce un datagram packet per inviare pacchetti di lunghezza i
                 * verso una porta specifita per uno specifico host
                 */
                DatagramPacket myPacket = new DatagramPacket(buffer, i, address,4000);
                //myPacket.setData(buffer);
                clientSocket.send(myPacket);
                Thread.sleep(200);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
