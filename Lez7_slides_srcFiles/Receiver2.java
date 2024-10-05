package Lez7_slides_srcFiles;

import java.net.*;
public class Receiver2 {
    public static void main(String[] args) throws Exception{
        DatagramSocket serverSocket = new DatagramSocket(40000);
        byte[] buffer = new byte[100];
        DatagramPacket receivedPacket = new DatagramPacket(buffer,buffer.length);

        while(true){
            serverSocket.receive(receivedPacket);
            String byteToString = new String(receivedPacket.getData(),0,receivedPacket.getLength(),"US-ASCII");
            int l = byteToString.length();
            System.out.println(l);
            System.out.println("Lenght " + receivedPacket.getLength() + " data " + byteToString );
        }
    }
}
