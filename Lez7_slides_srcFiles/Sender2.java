package Lez7_slides_srcFiles;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sender2 {
    public static void main (String[] args){
        try(DatagramSocket clientSocket = new DatagramSocket()){
            byte[] buffer = "1234567890abcdefghijklmnopqrstuvwxyz".getBytes("US-ASCII");
            InetAddress address = InetAddress.getByName("127.0.0.1");
            for(int i = buffer.length; i > 0; i--){
                DatagramPacket myPacket = new DatagramPacket(buffer,i,address,40000);
                clientSocket.send(myPacket);
                Thread.sleep(200);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
