package Lez7_slides_srcFiles;
import java.net.*;

/**
 * Questa classe usa uno Scanner per porte UDP
 */
public class ScannerPorte {
    public static void main(String[] args){
        for(int initialPort = 1024; initialPort < 2000; initialPort++){
            try{
                DatagramSocket s = new DatagramSocket(initialPort);
                System.out.println("Port that is free: " + initialPort);
                s.close();
            }catch (BindException e){
                System.out.println("Port is used");
            }
            catch (Exception e){
                System.out.println(e);
            }
        }
    }
}
