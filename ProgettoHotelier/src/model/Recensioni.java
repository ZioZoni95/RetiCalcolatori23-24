package model;

import java.time.LocalDateTime;

public class Recensioni {
    private String username;
    private int hotelID;
    private int rate;
    private HotelRate rating;
    private String timestamp;

    public Recensioni(){}

    public Recensioni(String username, int hotelID, int rate, HotelRate rating){
        this.username = username;
        this.hotelID = hotelID;
        this.rate = rate;
        this.rating = new HotelRate(rating);

        //ottendo la data e l'ora di quando è stata messa la recensione
        timestamp = LocalDateTime.now().toString();
    }

    public String getUsername() {
        return username;
    }

    public int gethotelID() {
        return hotelID;
    }

    public int getRate() {
        return rate;
    }

    public HotelRate getRating() {
        return new HotelRate(rating);
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Username: ").append(username).append("\n");
        builder.append("HotelID: ").append(hotelID).append("\n");
        builder.append("Rate: ").append(rate).append("\n");
        builder.append("Review: ").append(rating).append("\n");
        builder.append("Timestamp: ").append(timestamp);

        return builder.toString();
    }

}