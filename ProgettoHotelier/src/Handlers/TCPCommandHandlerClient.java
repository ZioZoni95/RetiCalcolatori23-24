package Handlers;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPCommandHandlerClient {
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectMapper objectMapper;

    public TCPCommandHandlerClient () throws Exception{
        HotelierClientConfigManager.ClientConfig clientConfig = HotelierClientConfigManager.getClientConfig();
        this.socket = new Socket(client.c)


    }
}
