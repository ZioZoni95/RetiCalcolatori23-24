
import java.net.*;
public class ScannerPorte {

	public static void main(String args[]) {
		for (int i=1024; i<2000; i++) {
			try {
				DatagramSocket s =new DatagramSocket(i);
				System.out.println ("Porta libera "+i);
				s.close();
			}
			catch (BindException e) {
				System.out.println("porta già in uso");
			}
			catch (Exception e) {
				System.out.println(e);
			}
		} 
	}
}
