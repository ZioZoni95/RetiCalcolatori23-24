package client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * modello secondo JAVA I/O con threadpool
 */
public class HotelierClientTCP {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args){
        try(Socket socket = new Socket(SERVER_ADDRESS,SERVER_PORT);
            BufferedReader bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            Scanner scanner = new Scanner(System.in)){

            System.out.println("Connesso al server");

            while (true){
                System.out.println("Inserisci il comando tra quelli disponibili ");
                String command = scanner.nextLine();

                if("exit".equalsIgnoreCase(command)){
                    break;
                }

                out.println(command);
                String response = bufferIn.readLine();
                System.out.println("Risposta dal server " + response);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
