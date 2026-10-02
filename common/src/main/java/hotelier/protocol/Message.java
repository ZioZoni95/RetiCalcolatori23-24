package hotelier.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import hotelier.model.Badge;
import hotelier.model.Hotel;
import hotelier.model.Ratings;

import java.util.List;

/**
 * Messaggi scambiati su TCP tra client e server. Ogni messaggio è un oggetto JSON con
 * il campo {@code type} che ne identifica il tipo; il trasporto è descritto in {@link Wire}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Message.LoginRequest.class, name = "login"),
        @JsonSubTypes.Type(value = Message.LogoutRequest.class, name = "logout"),
        @JsonSubTypes.Type(value = Message.SearchHotelRequest.class, name = "search_hotel"),
        @JsonSubTypes.Type(value = Message.SearchCityRequest.class, name = "search_city"),
        @JsonSubTypes.Type(value = Message.InsertReviewRequest.class, name = "insert_review"),
        @JsonSubTypes.Type(value = Message.BadgeRequest.class, name = "badge"),
        @JsonSubTypes.Type(value = Message.Success.class, name = "success"),
        @JsonSubTypes.Type(value = Message.Failure.class, name = "failure"),
        @JsonSubTypes.Type(value = Message.HotelResult.class, name = "hotel_result"),
        @JsonSubTypes.Type(value = Message.HotelListResult.class, name = "hotel_list_result"),
        @JsonSubTypes.Type(value = Message.BadgeResult.class, name = "badge_result")
})
public sealed interface Message {

    /** Messaggi inviati dal client. */
    sealed interface Request extends Message {
    }

    /** Messaggi inviati dal server in risposta a una richiesta. */
    sealed interface Response extends Message {
    }

    record LoginRequest(String username, String password) implements Request {
    }

    record LogoutRequest() implements Request {
    }

    record SearchHotelRequest(String hotelName, String city) implements Request {
    }

    record SearchCityRequest(String city) implements Request {
    }

    record InsertReviewRequest(String hotelName, String city, int rate, Ratings ratings) implements Request {
    }

    record BadgeRequest() implements Request {
    }

    record Success(String message) implements Response {
    }

    record Failure(String message) implements Response {
    }

    record HotelResult(Hotel hotel) implements Response {
    }

    record HotelListResult(List<Hotel> hotels) implements Response {
    }

    record BadgeResult(Badge badge) implements Response {
    }
}
