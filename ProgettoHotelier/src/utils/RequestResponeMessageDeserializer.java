package utils;

import Response_Request_netPackets.Request_ResponseMessage;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import Response_Request_netPackets.*;

/**
 * La classe `RequestResponeMessageDeserializer` è un deserializzatore personalizzato per la deserializzazione di
 * oggetti di tipo `Request_ResponseMessage`. Questa classe estende `JsonDeserializer<Request_ResponseMessage>`,
 * permettendo di interpretare diverse strutture di messaggi JSON in base ai loro campi specifici.
 */
public class RequestResponeMessageDeserializer extends JsonDeserializer<Request_ResponseMessage> {

    /**
     * Metodo che deserializza un oggetto JSON in un'istanza di `Request_ResponseMessage`, utilizzando il
     * tipo di messaggio determinato dai campi contenuti nel JSON.
     *
     * @param parser il parser JSON che fornisce il flusso di input da deserializzare
     * @param context il contesto di deserializzazione (non utilizzato direttamente qui)
     * @return l'oggetto deserializzato di tipo `Request_ResponseMessage` (specifico a seconda del contenuto JSON)
     * @throws IOException se si verifica un errore durante la deserializzazione
     */
    @Override
    public Request_ResponseMessage deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        // Legge l'albero JSON dalla posizione corrente del parser
        JsonNode node = parser.getCodec().readTree(parser);

        // Verifica quale tipo di messaggio JSON è presente ispezionando i campi
        // Se il nodo JSON ha i campi "username" e "password", si tratta di una richiesta di login
        if (node.has("username") && node.has("password")) {
            // Deserializza il nodo JSON in un oggetto di tipo `loginMessageRequest`
            return parser.getCodec().treeToValue(node, loginMessageRequest.class);

            // Se il nodo ha i campi "hotelName" e "city" (e possibilmente "rate" e "ratings"), si tratta di una richiesta di recensione
        } else if (node.has("hotelName") && node.has("city")) {
            // Se il nodo ha anche "rate" e "ratings", deserializza come un messaggio di inserimento recensione
            if (node.has("rate") && node.has("ratings")) {
                return parser.getCodec().treeToValue(node, insertReviewRequestMessage.class); // Messaggio con valutazione e recensioni
            }
            // Altrimenti, deserializza come una richiesta di ricerca hotel
            return parser.getCodec().treeToValue(node, searchHotelMessageRequest.class);

            // Se il nodo ha il campo "city", si tratta di una richiesta di ricerca di tutti gli hotel in una città
        } else if (node.has("city")) {
            // Deserializza come una richiesta di ricerca per tutti gli hotel
            return parser.getCodec().treeToValue(node, searchAllHotelsMessage.class);

            // Se il nodo ha il campo "badgeLevel", si tratta di una richiesta di livello badge
        } else if (node.has("badgeLevel")) {
            // Deserializza come una richiesta per il badge
            return parser.getCodec().treeToValue(node, badgeLevelMessageRequest.class);
        }

        // Se nessuna delle condizioni precedenti è soddisfatta, l'oggetto JSON non corrisponde a nessun tipo conosciuto
        // In tal caso, viene generata un'eccezione per segnalare una struttura di messaggio sconosciuta
        throw new IOException("Unknown message structure: " + node.toString());
    }
}
