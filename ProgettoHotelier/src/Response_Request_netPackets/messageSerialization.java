package Response_Request_netPackets;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
public class messageSerialization {
    /**
     * messageSerializzazion è una classe statica che gestice la deserializzazione e l'identificazione
     * dei pacchetti di rete
     */
    // Istanzio l'ObjectMapper di Jackson come statico, in modo che venga utilizzato per tutte le operazioni di serializzazione/deserializzazione
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Restituisce il pacchetto deserializzato rispetto all'ID pacchetto passato, o null se il pacchetto non è supportato
    public static Request_ResponseMessage getPacketFromID(int packetID, ByteBuffer packetPayload) {

        // Converte il ByteBuffer passato in una stringa
        String serializedPacket = new String(packetPayload.array(), StandardCharsets.UTF_8);

        // Usa un blocco try-catch per la gestione di errori di deserializzazione
        try {
            // Filtra in base all'ID pacchetto e usa Jackson per deserializzare la stringa JSON nel tipo corretto
            return switch (packetID) {
                case 0 -> objectMapper.readValue(serializedPacket, loginMessageRequest.class);
                case 1 -> objectMapper.readValue(serializedPacket, loginResponseMessage.class);
                case 2 -> objectMapper.readValue(serializedPacket, logoutMessageRequest.class);
                case 3 -> objectMapper.readValue(serializedPacket, logoutResponseMessage.class);
                case 4 -> objectMapper.readValue(serializedPacket, searchHotelMessageRequest.class);
                case 5 -> objectMapper.readValue(serializedPacket, searchHotelResponseMessage.class);
                case 6 -> objectMapper.readValue(serializedPacket, searchAllHotelsMessage.class);
                case 7 -> objectMapper.readValue(serializedPacket, searchAllHotelsResponseMessage.class);
                case 8 -> objectMapper.readValue(serializedPacket, insertReviewRequestMessage.class);
                case 9 -> objectMapper.readValue(serializedPacket, insertReviewResponseMessage.class);
                case 10 -> objectMapper.readValue(serializedPacket,badgeLevelMessageRequest.class);
                case 11 -> objectMapper.readValue(serializedPacket, badgeLevelMessageResponse.class);
                case 12 -> objectMapper.readValue(serializedPacket, errorResponseMessage.class);
                default -> null;
            };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Restituisce l'ID del pacchetto relativo al pacchetto passato, o -1 se il pacchetto non è supportato
    public static int getIDFromPacket(Request_ResponseMessage packet) {
        // Filtra in base all'istanza del pacchetto
        return switch (packet) {
            case loginMessageRequest loginPacket -> 0;
            case loginResponseMessage loginResponsePacket -> 1;
            case logoutMessageRequest logoutPacket -> 2;
            case logoutResponseMessage logoutResponsePacket -> 3;
            case searchHotelMessageRequest hotelPacket -> 4;
            case searchHotelResponseMessage hotelResponsePacket -> 5;
            case searchAllHotelsMessage hotelListPacket -> 6;
            case searchAllHotelsResponseMessage hotelListResponsePacket -> 7;
            case insertReviewRequestMessage reviewPacket -> 8;
            case insertReviewResponseMessage reviewResponsePacket -> 9;
            case badgeLevelMessageRequest badgePacket -> 10;
            case badgeLevelMessageResponse badgeResponsePacket -> 11;
            case errorResponseMessage errorResponseRequest -> 12;
            default -> -1;
        };
    }
}
