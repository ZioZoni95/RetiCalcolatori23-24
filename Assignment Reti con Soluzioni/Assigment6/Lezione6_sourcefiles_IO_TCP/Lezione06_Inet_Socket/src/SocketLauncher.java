
import java.net.*;  import java.io.*;

public class SocketLauncher {

	public static void main(String args[]) {
		String host;
		host = "localhost"; 
		for (int port=1;port<=1024; port++) {

			try {
				ServerSocket server = new ServerSocket(port);
				System.out.println(port + "in ascolto");
			}
			catch (BindException ex)
			{System.out.println(port + "occupata");}                        
			catch (Exception ex) {
				System.out.println(ex);
			}  
		}


	}

}


