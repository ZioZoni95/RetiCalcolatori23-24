package model;

import java.util.ArrayList;
import java.util.List;

public class LocalHotelRanking {

    private  String city;
    private List<Hotel> hotels;

    public LocalHotelRanking(){}

    public LocalHotelRanking(String city) {
        this.city = city;
        this.hotels = new ArrayList<>();

    }

    public void add(Hotel hotel) {
        hotels.add(hotel);
    }

    public String getCity() {
        return city;
    }

    public List<Hotel> getHotels() {
        return hotels;
    }

    public void setHotels(List<Hotel> hotels) {

        this.hotels = hotels;

    }

}
