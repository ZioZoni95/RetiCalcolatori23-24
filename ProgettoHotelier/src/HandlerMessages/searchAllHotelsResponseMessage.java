package HandlerMessages;

import model.Hotel;

import java.util.List;

public class searchAllHotelsResponseMessage extends Request_ResponseMessage {
    private final List<Hotel> hotels;

    public searchAllHotelsResponseMessage(List<Hotel> hotels){
        this.hotels = hotels;
    }
    public List<Hotel> getHotels(){
        return hotels;
    }
}
