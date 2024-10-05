package Lez7_slides_srcFiles;
import java.net.*;
import java.io.IOException;

public class Receiver {
    public static void main(String[] args){
        try(DatagramSocket myServerSocket = new DatagramSocket(4000)){
            myServerSocket.setSoTimeout(2000);
            byte[] buffer = new byte[100];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            while(true){
                myServerSocket.receive(receivePacket);
                String byteToString = new String(receivePacket.getData(), 0, receivePacket.getLength(),
                        "US_ASCII");
                System.out.println("Lenght " + receivePacket.getLength() + " data " + byteToString);
            }
        }catch (SocketTimeoutException e){
            System.out.println("Socket Timeout");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
