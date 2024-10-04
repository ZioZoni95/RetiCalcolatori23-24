package Lez6_slides_srcFiles;
import java.net.*;
import java.security.Security;
import java.io.*;
/**
 * Port scanner: individuazione servizi su server
 * Analisi
 *      --il client richiede la connessione tentando di creare un socket su ognuna delle prime 1024 porte di un host
 *      --nel caso in cui non vi sia alcun servizio attivo, il socket non viene creato => Throw Exception
 *
 */

public class PortScanner {
    public static void main(String[] args){
        String host;
        try{
            host = args[0];
        }
        catch (ArrayIndexOutOfBoundsException e){
            host = "localhost";
        }
        for (int i = 80; i<1024; i++)
            try(Socket s = new Socket(host,i)){
                System.out.println("Service already exist on this port " + i);
            }catch (UnknownHostException e1){
                System.out.println("Unknow Host");
                break;
            }catch (IOException e){
                System.out.println("There isn't any service on this port " + i);
            }
    }
}
