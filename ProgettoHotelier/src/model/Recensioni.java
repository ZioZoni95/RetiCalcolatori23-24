package model;

import java.time.LocalDateTime;

public class Recensioni {
    private final String username;
    private final int idHotel;
    private final int rateScore;
    private final HotelRate rating;
    private final String timestamp;

    public Recensioni(String username, int idHotel, int rateScore, HotelRate rating){
        this.username = username;
        this.idHotel = idHotel;
        this.rateScore = rateScore;
        this.rating = new HotelRate(rating);

        //ottendo la data e l'ora di quando è stata messa la recensione
        timestamp = LocalDateTime.now().toString();
    }

    public String getUsername() {
        return username;
    }

    public int gethotelID() {
        return idHotel;
    }

    public int getRate() {
        return rateScore;
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
        builder.append("HotelID: ").append(idHotel).append("\n");
        builder.append("Rate: ").append(rateScore).append("\n");
        builder.append("Review: ").append(rating).append("\n");
        builder.append("Timestamp: ").append(timestamp);

        return builder.toString();
    }

}