package Response_Request_netPackets;

public class searchHotelMessageRequest extends Request_ResponseMessage {
    private String hotelName;
    private String city;

    public searchHotelMessageRequest(){}

    public searchHotelMessageRequest(String hotelName, String city){
        this.hotelName = hotelName;
        this.city = city;
    }

    public String getCity(){
        return city;
    }
    public String getHotelName(){
        return hotelName;
    }
}
