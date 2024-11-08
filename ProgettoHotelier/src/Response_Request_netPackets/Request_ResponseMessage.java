package Response_Request_netPackets;
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
        @JsonSubTypes.Type(value = searchHotelMessageRequest.class, name = "hotel_request"),
        @JsonSubTypes.Type(value = searchHotelResponseMessage.class, name = "hotel_response"),
        @JsonSubTypes.Type(value = logoutMessageRequest.class, name = "logout_Request"),
        @JsonSubTypes.Type(value = logoutResponseMessage.class, name = "logout_Response"),
        @JsonSubTypes.Type(value = insertReviewRequestMessage.class, name = "ins_reviewRequest"),
        @JsonSubTypes.Type(value = insertReviewResponseMessage.class, name = "ins_reviewResponse"),
        @JsonSubTypes.Type(value = badgeLevelMessageRequest.class, name = "badgeLvl_Request"),
        @JsonSubTypes.Type(value = badgeLevelMessageResponse.class, name = "badgeLvl_Response"),
        @JsonSubTypes.Type(value = errorResponseMessage.class, name = "error_Response")
})

public abstract class Request_ResponseMessage {
   // public Request_ResponseMessage(){

    }


