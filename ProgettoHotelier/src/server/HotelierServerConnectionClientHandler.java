package server;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import Response_Request_netPackets.*;
import com.fasterxml.jackson.databind.ObjectMapper;


public class HotelierServerConnectionClientHandler {

    // canale associato al client
    private final SocketChannel client;
    // handler dei pacchetti
    private final HotelierServerMessageManager hotelierMessageManager;
    // coda pacchetti di risposta
    private final Queue<Request_ResponseMessage> responseQueue;

    // byte buffer per lettura delle richiesta e scrittura delle risposte
    private ByteBuffer requestHeader, requestPayload;
    private ByteBuffer responseBuffer;

    // booleano per gestire la connessione del client
    private boolean isConnected;

    // Jackson ObjectMapper for JSON serialization/deserialization
    private final ObjectMapper objectMapper;

    public HotelierServerConnectionClientHandler(SocketChannel client) {
        this.client = client;
        hotelierMessageManager = new HotelierServerMessageManager();
        responseQueue = new LinkedList<>();
        isConnected = true;

        // Initialize Jackson ObjectMapper
        objectMapper = new ObjectMapper();
    }

    // restituisce il pacchetto di richiesta inviato dal client
    public Request_ResponseMessage handleRead() {
        if (isConnected) {
            try {
                if (requestHeader == null) {
                    requestHeader = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
                }

                if (requestHeader.hasRemaining()) {
                    if (client.read(requestHeader) == -1) {
                        throw new IOException();
                    }
                }

                if (!requestHeader.hasRemaining() && requestPayload == null) {
                    requestHeader.flip();
                    int payloadSize = requestHeader.getInt();
                    requestPayload = ByteBuffer.allocate(payloadSize).order(ByteOrder.BIG_ENDIAN);
                }

                if (requestPayload != null && requestPayload.hasRemaining()) {
                    if (client.read(requestPayload) == -1) {
                        throw new IOException("<Errore di lettura>: client disconnesso durante la lettura del payload");
                    }
                }

                if (requestPayload != null && !requestPayload.hasRemaining()) {
                    int packetID = requestHeader.getInt();
                    requestPayload.flip();
                    Request_ResponseMessage packet = deserializeRequest(packetID, requestPayload);
                    requestHeader = null;
                    requestPayload = null;
                    return packet;
                }
            } catch (IOException exception) {
                isConnected = false;
            }
        }
        return null;
    }

    public void handlePacket(Request_ResponseMessage packet) {
        Request_ResponseMessage responsePacket = hotelierMessageManager.handlePacket(packet);
        if (responsePacket != null) {
            synchronized (responseQueue) {
                responseQueue.add(responsePacket);
            }
        }
    }

    public void handleWrite() {
        if (isConnected) {
            try {
                if (responseBuffer == null) {
                    synchronized (responseQueue) {
                        Request_ResponseMessage responsePacket = responseQueue.peek();
                        if (responsePacket != null) {
                            responseBuffer = serializeResponse(responsePacket);
                        }
                    }
                }

                if (responseBuffer != null) {
                    client.write(responseBuffer);
                    if (!responseBuffer.hasRemaining()) {
                        synchronized (responseQueue) {
                            responseQueue.poll();
                        }
                        responseBuffer = null;
                    }
                }

            } catch (IOException exception) {
                isConnected = false;
            }
        }
    }

    private ByteBuffer serializeResponse(Request_ResponseMessage packet) {
        try {
            int packetID = messageSerialization.getIDFromPacket(packet);
            if (packetID != -1) {
                byte[] serializedPacket = objectMapper.writeValueAsBytes(packet);
                int payloadSize = serializedPacket.length;
                ByteBuffer serializedResponse = ByteBuffer.allocate(4 + 4 + payloadSize).order(ByteOrder.BIG_ENDIAN);
                serializedResponse.putInt(payloadSize);
                serializedResponse.putInt(packetID);
                serializedResponse.put(serializedPacket);
                serializedResponse.flip();
                return serializedResponse;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Request_ResponseMessage deserializeRequest(int packetID, ByteBuffer requestPayload) {
        byte[] payloadBytes = new byte[requestPayload.remaining()];
        requestPayload.get(payloadBytes);
        try {
            // Directly deserialize JSON to Request_ResponseMessage using ObjectMapper
            Request_ResponseMessage packet = objectMapper.readValue(payloadBytes, Request_ResponseMessage.class);

            // Optionally validate packetID or use packetID to determine specific type if needed
            return packet;
        } catch (IOException e) {
            System.err.println("Error deserializing request: " + e.getMessage());
            return null;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void close() throws IOException {
        hotelierMessageManager.handleClientDisconnect();
        client.close();
    }
}
