import java.io.*;
import java.net.*;

public class Multidatastreamsender{
	public static void main(String args[ ]) throws Exception {
		// fase di inizializzazione
		InetAddress ia=InetAddress.getByName("localhost");
		int port=13350;
		DatagramSocket ds= new DatagramSocket();
		ByteArrayOutputStream bout= new ByteArrayOutputStream();	       	
		DataOutputStream dout = new DataOutputStream (bout);  	
		byte[] data = new byte[20];
		DatagramPacket dp= new DatagramPacket(data, data.length, ia,port);
		for (int i=0; i< 10; i++) {
			dout.writeInt(i);
			data = bout.toByteArray();
			dp.setData(data,0,data.length);
			dp.setLength(data.length);
			ds.send(dp);
			bout.reset( );
			dout.writeUTF("***");
			data = bout.toByteArray( );
			dp.setData (data,0,data.length);
			dp.setLength (data.length);
			ds.send (dp);
			bout.reset();
		} 
	}
}
