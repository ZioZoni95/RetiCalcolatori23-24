package HandlerMessages;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
/**
 * Questa classe si occupa della gestione dei messaggi trasmessi relativi ai vari comandi disponibili
 *
 */

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // This field will indicate the class type in JSON
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = loginMessageRequest.class, name = "login_request"),
        @JsonSubTypes.Type(value = loginResponseMessage.class, name = "login_response"),
        @JsonSubTypes.Type(value = searchAllHotelsMessage.class, name = "all_hotels_request"),
        @JsonSubTypes.Type(value = searchAllHotelsResponseMessage.class, name = "all_hotels_response"),
        @JsonSubTypes.Type(value = insertReviewRequestMessage.class, name = "ins_reviewRequest"),
        @JsonSubTypes.Type(value = insertReviewResponseMessage.class, name = "ins_reviewResponse")


        // Add other concrete subclasses as needed
})

public abstract class Request_ResponseMessage {
   // public Request_ResponseMessage(){

    }


