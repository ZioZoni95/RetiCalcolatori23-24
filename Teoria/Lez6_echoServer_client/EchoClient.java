package Lez6_echoServer_client;

import java.io.*;
import java.net.*;

public class EchoClient {
    public static void main(String[] args){
        Socket socket = new Socket();
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try{
            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(),1500));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader localReader = new BufferedReader(new BufferedReader(new InputStreamReader(System.in)));
            System.out.println("Type 'exit' to stop, any message to sendo to server : ");
            String reply = "";
            String choice;
            while(!(choice = localReader.readLine().trim()).equals("exit")){
                writer.write(choice + "\r\n");
                writer.flush();
                reply = reader.readLine();
                System.out.println("Server sent back: " + reply + "");
            }
            socket.close();
        }
        catch (SocketException e1){
            System.out.println("Server closed connection or and error appeared.");
        }
        catch (UnknownHostException e2){
            e2.printStackTrace();
        }
        catch (IOException e3){
            System.out.println("Server closed connection or and error appeared.");
        }
    }
}
