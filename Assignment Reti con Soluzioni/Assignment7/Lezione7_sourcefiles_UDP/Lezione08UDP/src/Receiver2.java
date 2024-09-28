import java.net.*;
public class Receiver2 {
	public static void main(String args[]) throws Exception {
		DatagramSocket serverSock= new DatagramSocket(40000);
		byte[] buffer = new byte[100];
		DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
		
		while (true) {		
			serverSock.receive(receivedPacket);
			String byteToString = new String(receivedPacket.getData(),"US-ASCII");
			//String byteToString = new String(receivedPacket.getData(),  receivedPacket.getOffset(), receivedPacket.getLength(), "US-ASCII");
			int l= byteToString.length();
			System.out.println(l);
			System.out.println("Length " + receivedPacket.getLength() +                                      	                                     " data " + byteToString);
		}
	}
}
