package Lez6_echoServer_client;
import java.net.*;
import java.io.*;

public class EchoServer {
    public static void main(String[] args){
        try(ServerSocket server = new ServerSocket();){ //provo ad inizializzare il server
            server.bind(new InetSocketAddress(InetAddress.getLocalHost(),1500));
            while(true){
                System.out.println("In attesa dei client");
                String message;
                try(Socket client = server.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));){
                    while((message = reader.readLine()) != null){
                        System.out.println("Client sent: " + message);
                        writer.write(message + "\r\n");
                        writer.flush();
                        /*if((message = reader.readLine().trim()).equals("exit")){
                            client.close();
                            server.close();
                            break;
                        }*/
                    }
                }catch(IOException e){
                    System.out.println("Client closed connection or error appeared ");
                }
            }
        }
        catch (UnknownHostException e2){
            e2.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
/**
 *
 * il server è attivo, manca da impostare la chiusura del socket e l'arresto del server
 */