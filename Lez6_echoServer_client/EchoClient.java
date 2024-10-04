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
            System.out.println();
        }
    }
}
