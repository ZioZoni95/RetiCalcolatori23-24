package Response_Request_netPackets;

import model.Hotel;

import java.util.List;

public class searchAllHotelsResponseMessage extends Request_ResponseMessage {
    private  List<Hotel> hotels;

    public searchAllHotelsResponseMessage() {}
    public searchAllHotelsResponseMessage(List<Hotel> hotels){
        this.hotels = hotels;
    }
    public List<Hotel> getHotels(){
        return hotels;
    }
}
