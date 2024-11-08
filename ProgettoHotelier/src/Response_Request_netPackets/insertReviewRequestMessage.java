package Response_Request_netPackets;

import model.HotelRate;

public class insertReviewRequestMessage extends Request_ResponseMessage {
    private  String hotelName;
    private  String city;
    private  int rate;
    private  HotelRate ratings;

    public insertReviewRequestMessage(){}

    public insertReviewRequestMessage(String hotelName, String city, int rate, HotelRate ratings){
        this.hotelName = hotelName;
        this.city = city;
        this.rate = rate;
        this.ratings = ratings;
    }

    public String getHotelName() {
        return hotelName;
    }

    public String getCity() {
        return city;
    }

    public int getRate() {
        return rate;
    }

    public HotelRate getRatings() {
        return ratings;
    }
}
