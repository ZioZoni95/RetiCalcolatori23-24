package HandlerMessages;

import model.Hotel;

public class searchHotelResponseMessage extends Request_ResponseMessage {
    private final Hotel hotels;

    public searchHotelResponseMessage(Hotel hotels){
        this.hotels = hotels;
    }

    public Hotel getHotels(){
        return hotels;
    }
}
