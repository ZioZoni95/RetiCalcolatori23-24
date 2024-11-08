package Response_Request_netPackets;

import model.Hotel;

public class searchHotelResponseMessage extends Request_ResponseMessage {
    private Hotel hotels;

    public searchHotelResponseMessage(){}

    public searchHotelResponseMessage(Hotel hotels){
        this.hotels = hotels;
    }

    public Hotel getHotels(){
        return hotels;
    }
}
