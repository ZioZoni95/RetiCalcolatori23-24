import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class SocketProps {
public static void main(String[] args) {

	try (Socket socket = new Socket()) { 
		SocketAddress address = new InetSocketAddress("www.google.com", 80);
		socket.connect(address);
		System.out.println("Connected to " + socket.getInetAddress()
			+ " on port " + socket.getPort() + " from port "
			+ socket.getLocalPort() + " of " + socket.getLocalAddress());
		}
 	catch (UnknownHostException ex) {
		System.err.println("I can't find the host ");}
 catch (IOException ex) {
		System.err.println(ex);
	} 
	
 } 
}
