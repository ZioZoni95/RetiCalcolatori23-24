package Lez6_slides_srcFiles;
import java.net.*;
import java.io.*;

/**
 * 
 */

public class SocketLauncher  {
    public static void main(String[] args){
        String host;
        host = "www.google.com";
        for(int port = 0; port <= 1024; port++){
            try{
                ServerSocket server = new ServerSocket(port);
                System.out.println(port + " Listening... ");
            }
            catch(BindException e1){
                System.out.println(port + " Port is busy ");
            }
            catch (Exception e2){
                System.out.println(e2);
            }
        }
    }
}
