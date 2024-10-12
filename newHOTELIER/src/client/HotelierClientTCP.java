package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * modello secondo JAVA I/O con threadpool
 */
public class HotelierClientTCP {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8081;

    public void startClient(){
        Socket socket = new Socket();
        /*vecchio try debug
        try(Socket socket = new Socket(SERVER_ADDRESS,SERVER_PORT);
            BufferedReader bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            Scanner scanner = new Scanner(System.in)){
            */
        BufferedReader buffIn = null;
        BufferedWriter outBuff = null;
        try{
            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(),8080));
            buffIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outBuff = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader localReader = new BufferedReader(new BufferedReader(new InputStreamReader(System.in)));
            /*
            * fine debug
            *
            */

            System.out.println("Connesso al server TCP");

            while (true){
                //richiedi comando all'utente
                System.out.println("Inserisci il comando tra quelli disponibili ");
                String command = localReader.readLine().trim();

                if("exit".equalsIgnoreCase(command)){
                    break;
                }
                outBuff.write(command);
                outBuff.flush(); //invia comando al server
                String response = buffIn.readLine();
                System.out.println("Risposta dal server " + response);
            }
            socket.close();

        }catch (SocketException e1){
            System.out.println("socket error");
        }catch (UnknownHostException e2){
            e2.printStackTrace();
        }catch(IOException e){
            System.out.println("IOException : connection closed or error");
        }
    }
}
