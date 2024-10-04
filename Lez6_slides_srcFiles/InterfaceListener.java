package Lez6_slides_srcFiles;


import java.net.*;

public class InterfaceListener {
    public static void main(String[] args) throws Exception{
        try {
            InetAddress local = InetAddress.getByName("127.0.0.1");
            NetworkInterface ni = NetworkInterface.getByInetAddress(local);
            if (ni == null) {
                System.err.println("No local loopback address");
            } else {
                System.out.println(ni);
            }
        }
        catch(UnknownHostException e){
            System.err.println("No local loopback address");
        }
    }
}
