package Lez6_slides_srcFiles;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class SocketPros {
    public static void main(String[] args){
        try(Socket socket = new Socket()){
            SocketAddress address = new InetSocketAddress("www.github.com",80);
            socket.connect(address);
            System.out.println("Connected to " + socket.getInetAddress() + " on port " + socket.getPort()
                + " from port " + socket.getLocalPort() + " of " + socket.getLocalAddress());
        }
        catch(UnknownHostException e1){
            System.err.println("Cant find the host ");
        }catch (IOException e2){
            System.err.println(e2);
        }
    }
}
