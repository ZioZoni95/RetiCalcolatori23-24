package client.ClientHandlers;
import HandlerMessages.*;
import HandlerMessages.Request_ResponseMessage;
import HandlerMessages.loginMessageRequest;
import client.HotelierCommands_Client;
import client.config.ClientConfigManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.Hotel;
import model.HotelRate;
import utils.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TCPCommandHandlerClient {
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectMapper objectMapper;

    public TCPCommandHandlerClient() throws Exception {
        var clientconfig = ClientConfigManager.getClientConfig();
        this.socket = new Socket(clientconfig.getServerAddress(), clientconfig.getTcpPort());
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String TCPCommandHandler(HotelierCommands_Client command) {

        try {
            Request_ResponseMessage requestPkt = createPacket(command);

            if (requestPkt != null) {
                sendPacket(requestPkt);
                Request_ResponseMessage responsePk = receivePacket();
                return getRespone(responsePk);
            }
        } catch (IOException e) {
            return "Errrore di Conessione: Impossibile stabilire connessione con server TCP di Hotelier";
        }
        return null;
    }

    private Request_ResponseMessage createPacket(HotelierCommands_Client command) {
        String c_name = command.getName();
        String[] c_args = command.getCommand_args();

        return switch (c_name) {
            case "login" -> createPacketLogin(c_args);
            case "logout" -> createPacketLogout();
            case "searchhotel" -> createPacketHotel(c_args);
            case "searchallhotels" -> createPacketAllHotels(c_args);
            case "insertreview" -> createPacketReview(c_args);
            case "showmybadges" -> createPacketBadgeLevel();
            default -> null;
        };
    }

    private void sendPacket(Request_ResponseMessage net_packet) throws IOException{
        int net_packetID = messageSerialization.getIDFromPacket(net_packet);
        String serializedPK = objectMapper.writeValueAsString(net_packet);
        byte[] packet_bytes = serializedPK.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + packet_bytes.length);
        buffer.putInt(packet_bytes.length);
        buffer.putInt(net_packetID);
        buffer.put(packet_bytes);
        outputStream.write(buffer.array());
        outputStream.flush();
    }

    private Request_ResponseMessage receivePacket() throws IOException {
        byte[] responseByte = new byte[8]; // header
        readAllBytes(responseByte);
        ByteBuffer responseHeader = ByteBuffer.wrap(responseByte);
        int payloadSize = responseHeader.getInt();
        int packet_id = responseHeader.getInt();

        byte[] responsePayloadBytes = new byte[payloadSize];
        readAllBytes(responsePayloadBytes);
        String jsonPayload = new String(responsePayloadBytes);

        try {
            return JsonUtils.deserializeRequestResponseMessage(jsonPayload);
        } catch (IOException e) {
            System.err.println("Error deserializing request: " + e.getMessage());
            throw e;
        }
    }

    private Request_ResponseMessage createPacketLogin(String[] c_args) {
        String username = c_args[0];
        String password = c_args[1];
        loginMessageRequest loginPkt = new loginMessageRequest(username, password);
        return loginPkt;
    }

    private Request_ResponseMessage createPacketLogout() {
        logoutMessageRequest logoutPkt = new logoutMessageRequest();
        return logoutPkt;
    }

    private void readAllBytes(byte[] byteArray) throws IOException {
        int totalBytesRead = 0;
        while (totalBytesRead < byteArray.length) {
            int bytesRead = inputStream.read(byteArray, totalBytesRead, byteArray.length - totalBytesRead);
            if (bytesRead == -1) {
                throw new IOException();
            }
            totalBytesRead += bytesRead;
        }
    }

    private Request_ResponseMessage createPacketHotel(String[] c_args){
        String hotelName = c_args[0];
        String city = c_args[1];
        return new searchHotelMessageRequest(hotelName,city);
    }

    private Request_ResponseMessage createPacketAllHotels(String[] c_args){
        String city = c_args[0];
        return new searchAllHotelsMessage(city);
    }

    private Request_ResponseMessage createPacketReview(String[] c_args){
        String hotelName = c_args[0];
        String city = c_args[1];
        int rate = Integer.parseInt(c_args[2]);
        int cleaning = Integer.parseInt(c_args[3]);
        int position = Integer.parseInt(c_args[4]);
        int services = Integer.parseInt(c_args[5]);
        int quality = Integer.parseInt(c_args[6]);
        HotelRate rating = new HotelRate(cleaning,position,services,quality);
        return new insertReviewRequestMessage(hotelName,city,rate,rating);
    }

    private Request_ResponseMessage createPacketBadgeLevel() {
        badgeLevelMessageRequest badge_packet = new badgeLevelMessageRequest();
        return badge_packet;
    }

    private String getRespone(Request_ResponseMessage net_packet) throws IOException{
        return switch (net_packet){
            case loginResponseMessage login_packet -> login_packet.getLoginResponse();
            case logoutResponseMessage logout_packet -> logout_packet.getLogoutResponse();
            case searchHotelResponseMessage hotel_packet -> hotel_packet.getHotels().toString();
            case searchAllHotelsResponseMessage allHotels_packet -> formatHotelList(allHotels_packet.getHotels());
            case insertReviewResponseMessage insertReview_packet -> insertReview_packet.getResponseMessage();
            case badgeLevelMessageResponse badge_packet -> badge_packet.getBadge().toString();
            case errorResponseMessage error_packet -> error_packet.getErrorMessageResponse();
            default -> null;
        };
    }

    private String formatHotelList(List<Hotel> hotels) {
        StringBuilder sb = new StringBuilder();
        String separator = "--------------------------------------------------\n";
        for (Hotel hotel : hotels) {
            sb.append(hotel).append("\n");
            sb.append(separator);
        }
        return sb.toString();
    }

    public void close() {
        try {
            if (!socket.isClosed()) {
                inputStream.close();
                outputStream.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

