


import java.net.*;
import java.security.Security;
import java.io.*;
public class PortScanner {
	public static void main(String args[]){
		String host;
		try {
			host = args[0]; 
		}
		catch (ArrayIndexOutOfBoundsException e) {
			host="www.google.com";   	// netcat su ubuntu VM oppure host esterno
		}
		for (int i=80;i<1024;i++)	
		try (Socket s = new Socket(host, i)){ 
								
				System.out.println("Esiste un servizio sulla porta "+i);}
			catch (UnknownHostException ex)
			{
				System.out.println("Host Sconosciuto"); break; }
			catch (IOException ex) {
				System.out.println("Non esiste un servizio sulla porta"+i);
			}
		}
	}

