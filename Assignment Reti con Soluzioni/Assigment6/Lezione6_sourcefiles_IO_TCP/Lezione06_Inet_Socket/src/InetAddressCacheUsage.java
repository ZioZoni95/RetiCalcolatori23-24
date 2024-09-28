

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.*;

public class InetAddressCacheUsage {
	public static void main(String[] args) throws UnknownHostException, SocketException {

	
		System.out.println(Security.getProperty("networkaddress.cache.ttl")); // null means unset defaulting to -1
		System.out.println(Security.getProperty("networkaddress.cache.negative.ttl"));
		final String CACHINGTIME="10000";	
		Security.setProperty("networkaddress.cache.ttl",CACHINGTIME);
		System.out.println(Security.getProperty("networkaddress.cache.ttl")); // null means unset defaulting to -1
		
		long time1 = System.currentTimeMillis();
		for (int i=0; i<1000; i++) {
			try { 
				System.out.println( i + " "+
						InetAddress.getByName("www.cnn.com").getHostAddress());}
			catch (UnknownHostException uhe) {
				System.out.println("UHE"); 
			}
		}
		long time2 = System.currentTimeMillis();
		long diff=time2-time1;
		System.out.println("tempo trascorso e'"+diff);
	}
}