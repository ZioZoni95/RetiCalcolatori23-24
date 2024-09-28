
import java.net.*;
public class Sender2 {
	public static void main (String args[]) {
		try (DatagramSocket clientSocket = new DatagramSocket()) {
			byte[] buffer="1234567890abcdefghijklmnopqrstuvwxyz".getBytes("US-ASCII");       
			InetAddress address = InetAddress.getByName("127.0.0.1");
			for (int i = buffer.length; i >0; i--) {
				DatagramPacket mypacket = new DatagramPacket(buffer,i,address,40000);
				clientSocket.send(mypacket);
				Thread.sleep(200);
			}                  
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
