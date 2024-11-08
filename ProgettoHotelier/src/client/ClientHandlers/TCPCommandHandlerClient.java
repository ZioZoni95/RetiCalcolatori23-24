package client.ClientHandlers;

import Response_Request_netPackets.*;
import Response_Request_netPackets.Request_ResponseMessage;
import Response_Request_netPackets.loginMessageRequest;
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
    private Socket socket; // Socket per la connessione TCP al server
    private OutputStream outputStream; // Stream di output per inviare dati al server
    private InputStream inputStream; // Stream di input per ricevere dati dal server
    private ObjectMapper objectMapper; // Utilizzato per serializzare e deserializzare i pacchetti JSON

    // Costruttore che inizializza la connessione TCP e i flussi di I/O
    public TCPCommandHandlerClient() throws Exception {
        var clientconfig = ClientConfigManager.getClientConfig(); // Recupera la configurazione del client
        this.socket = new Socket(clientconfig.getServerAddress(), clientconfig.getTcpPort()); // Crea il socket di connessione
        outputStream = socket.getOutputStream(); // Ottiene l'output stream dal socket
        inputStream = socket.getInputStream(); // Ottiene l'input stream dal socket
        objectMapper = new ObjectMapper(); // Crea l'ObjectMapper per la serializzazione JSON
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Abilita l'indentazione per la stampa dei JSON
    }

    // Gestisce il comando TCP ricevuto, crea il pacchetto appropriato e invia/riceve i dati
    public String TCPCommandHandler(HotelierCommands_Client command) {
        try {
            // Crea il pacchetto di richiesta a partire dal comando
            Request_ResponseMessage requestPkt = createPacket(command);

            if (requestPkt != null) {
                // Invia il pacchetto al server
                sendPacket(requestPkt);
                // Riceve la risposta dal server
                Request_ResponseMessage responsePk = receivePacket();
                // Elabora e restituisce la risposta come stringa
                return getRespone(responsePk);
            }
        } catch (IOException e) {
            // In caso di errore di connessione, restituisce un messaggio di errore
            return "Errrore di Conessione: Impossibile stabilire connessione con server TCP di Hotelier";
        }
        return null;
    }

    // Crea il pacchetto di richiesta appropriato in base al nome del comando
    private Request_ResponseMessage createPacket(HotelierCommands_Client command) {
        String c_name = command.getName(); // Nome del comando
        String[] c_args = command.getCommand_args(); // Argomenti del comando

        // Crea pacchetti diversi a seconda del comando
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

    // Invia il pacchetto al server
    private void sendPacket(Request_ResponseMessage net_packet) throws IOException{
        // Ottiene l'ID del pacchetto dal metodo di serializzazione
        int net_packetID = messageSerialization.getIDFromPacket(net_packet);
        // Serializza il pacchetto in una stringa JSON
        String serializedPK = objectMapper.writeValueAsString(net_packet);
        // Converte la stringa in un array di byte
        byte[] packet_bytes = serializedPK.getBytes(StandardCharsets.UTF_8);
        // Crea un buffer per il pacchetto, che include la lunghezza e l'ID
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + packet_bytes.length);
        buffer.putInt(packet_bytes.length); // Aggiunge la lunghezza del pacchetto
        buffer.putInt(net_packetID); // Aggiunge l'ID del pacchetto
        buffer.put(packet_bytes); // Aggiunge i dati del pacchetto
        // Scrive i dati nel flusso di output
        outputStream.write(buffer.array());
        outputStream.flush();
    }

    // Riceve un pacchetto dal server
    private Request_ResponseMessage receivePacket() throws IOException {
        byte[] responseByte = new byte[8]; // Array per leggere l'intestazione del pacchetto (lunghezza + ID)
        readAllBytes(responseByte); // Legge tutti i byte dell'intestazione
        ByteBuffer responseHeader = ByteBuffer.wrap(responseByte);
        // Estrae la lunghezza del payload e l'ID del pacchetto dall'intestazione
        int payloadSize = responseHeader.getInt();
        int packet_id = responseHeader.getInt();

        byte[] responsePayloadBytes = new byte[payloadSize]; // Array per i dati del payload
        readAllBytes(responsePayloadBytes); // Legge tutti i byte del payload
        String jsonPayload = new String(responsePayloadBytes); // Converte i byte in una stringa JSON

        try {
            // Deserializza il JSON nel pacchetto di risposta
            return JsonUtils.deserializeRequestResponseMessage(jsonPayload);
        } catch (IOException e) {
            System.err.println("Error deserializing request: " + e.getMessage());
            throw e;
        }
    }

    // Crea il pacchetto di login con i dati dell'utente
    private Request_ResponseMessage createPacketLogin(String[] c_args) {
        String username = c_args[0]; // Estrae il nome utente dagli argomenti
        String password = c_args[1]; // Estrae la password dagli argomenti
        loginMessageRequest loginPkt = new loginMessageRequest(username, password); // Crea il pacchetto di login
        return loginPkt;
    }

    // Crea il pacchetto di logout
    private Request_ResponseMessage createPacketLogout() {
        logoutMessageRequest logoutPkt = new logoutMessageRequest(); // Crea il pacchetto di logout
        return logoutPkt;
    }

    // Legge tutti i byte da un InputStream
    private void readAllBytes(byte[] byteArray) throws IOException {
        int totalBytesRead = 0;
        while (totalBytesRead < byteArray.length) {
            int bytesRead = inputStream.read(byteArray, totalBytesRead, byteArray.length - totalBytesRead);
            if (bytesRead == -1) {
                throw new IOException(); // Lancia eccezione se la lettura fallisce
            }
            totalBytesRead += bytesRead; // Aggiorna il numero di byte letti
        }
    }

    // Crea il pacchetto di ricerca per un hotel specifico
    private Request_ResponseMessage createPacketHotel(String[] c_args){
        String hotelName = c_args[0]; // Estrae il nome dell'hotel
        String city = c_args[1]; // Estrae la città
        return new searchHotelMessageRequest(hotelName, city); // Crea il pacchetto di ricerca
    }

    // Crea il pacchetto per la ricerca di tutti gli hotel in una città
    private Request_ResponseMessage createPacketAllHotels(String[] c_args){
        String city = c_args[0]; // Estrae la città
        return new searchAllHotelsMessage(city); // Crea il pacchetto di ricerca per tutti gli hotel
    }

    // Crea il pacchetto per l'inserimento di una recensione
    private Request_ResponseMessage createPacketReview(String[] c_args){
        String hotelName = c_args[0]; // Estrae il nome dell'hotel
        String city = c_args[1]; // Estrae la città
        int rate = Integer.parseInt(c_args[2]); // Estrae il voto complessivo
        int cleaning = Integer.parseInt(c_args[3]); // Estrae il voto sulla pulizia
        int position = Integer.parseInt(c_args[4]); // Estrae il voto sulla posizione
        int services = Integer.parseInt(c_args[5]); // Estrae il voto sui servizi
        int quality = Integer.parseInt(c_args[6]); // Estrae il voto sulla qualità
        HotelRate rating = new HotelRate(cleaning, position, services, quality); // Crea l'oggetto di valutazione
        insertReviewRequestMessage review_req_packet = new insertReviewRequestMessage(hotelName, city, rate, rating); // Crea il pacchetto di recensione
        return review_req_packet;
    }

    // Crea il pacchetto per ottenere il livello del badge
    private Request_ResponseMessage createPacketBadgeLevel() {
        badgeLevelMessageRequest badge_packet = new badgeLevelMessageRequest(); // Crea il pacchetto di richiesta del badge
        return badge_packet;
    }

    // Elabora la risposta e la restituisce come stringa
    private String getRespone(Request_ResponseMessage net_packet) throws IOException {
        return switch (net_packet){
            case loginResponseMessage login_packet -> login_packet.getLoginResponse(); // Risposta di login
            case logoutResponseMessage logout_packet -> logout_packet.getLogoutResponse(); // Risposta di logout
            case searchHotelResponseMessage hotel_packet -> hotel_packet.getHotels().toString(); // Risposta di ricerca per hotel specifico
            case searchAllHotelsResponseMessage allHotels_packet -> formatHotelList(allHotels_packet.getHotels()); // Risposta di ricerca per tutti gli hotel
            case insertReviewResponseMessage insertReview_packet -> insertReview_packet.getResponseMessage(); // Risposta di inserimento recensione
            case badgeLevelMessageResponse badge_packet -> badge_packet.getBadge().toString(); // Risposta del badge
            case errorResponseMessage error_packet -> error_packet.getErrorMessageResponse(); // Risposta di errore
            default -> null;
        };
    }

    // Formatta la lista di hotel in una stringa
    private String formatHotelList(List<Hotel> hotels) {
        StringBuilder sb = new StringBuilder();
        String separator = "--------------------------------------------------\n";
        for (Hotel hotel : hotels) {
            sb.append(hotel).append("\n");
            sb.append(separator); // Aggiunge un separatore tra gli hotel
        }
        return sb.toString();
    }

    // Chiude la connessione e i flussi
    public void close() {
        try {
            if (!socket.isClosed()) {
                inputStream.close();
                outputStream.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace(); // Stampa eventuali errori durante la chiusura
        }
    }
}
