package HandlerMessages;

import model.HotelRate;

public class insertReviewRequestMessage extends Request_ResponseMessage {
    private final String hotelname;
    private final String city;
    private final int rateScore;
    private final HotelRate ratings;

    public insertReviewRequestMessage(String hotelname, String city, int rateScore, HotelRate ratings){
        this.hotelname = hotelname;
        this.city = city;
        this.rateScore = rateScore;
        this.ratings = ratings;
    }

    public String getHotelName() {
        return hotelname;
    }

    public String getCity() {
        return city;
    }

    public int getRate() {
        return rateScore;
    }

    public HotelRate getRatings() {
        return ratings;
    }
}
