package model;


import java.util.ArrayList;
import java.util.List;

public class Hotel {
    private int id;
    private  String name;
    private  String description;
    private  String city;
    private  String phone;
    private  List<String> services;
    private float rate;
    private HotelRate ratings;
    private int reviewCount;
    private double rank;
    private int localRank;

    public Hotel() {}
    public Hotel(int id,String name, String description, String city, String phone, List<String> services, int rate,
                HotelRate ratings){
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = new ArrayList<>(services);
        this.rate = rate;
        this.ratings = new HotelRate(ratings);

    }

    // copy constructor, restitusce una copia dell' istanza hotel passata
    public Hotel(Hotel hotel) {
        this.id = hotel.id;
        this.name = hotel.name;
        this.description = hotel.description;
        this.city = hotel.city;
        this.phone = hotel.phone;
        this.services = new ArrayList<>(hotel.services);
        this.rate = hotel.rate;
        this.ratings = new HotelRate(hotel.ratings);
        this.reviewCount = hotel.reviewCount;
        this.rank = hotel.rank;
        this.localRank = hotel.localRank;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCity() {
        return city;
    }

    public String getPhone() {
        return phone;
    }

    public List<String> getServices() {
        return services;
    }

    public synchronized float getRate(){
        return rate;
    }

    public synchronized void setRate(float rate) {
        this.rate = rate;
    }

    public synchronized HotelRate getRatings() {
        return ratings;
    }

    public synchronized void setRatings(HotelRate ratings) {
        this.ratings = ratings;
    }

    public synchronized int getReviewCount() {
        return reviewCount;
    }


    public synchronized void setReviewCount(int reviewCount){
        this.reviewCount = reviewCount;
    }

    public synchronized void incrementReviews(){
        this.reviewCount++;
    }

    public synchronized double getRank(){
        return rank;
    }

    public synchronized void setRankLevel(double rank){
        this.rank = rank;
    }

    public synchronized void setLocalRanking(int localRank){
        this.localRank = localRank;
    }

    public synchronized int getLocalRank() {
        return localRank;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Nome: ").append(name).append("\n");
        sb.append("Descrizione: ").append(description).append("\n");
        sb.append("Città: ").append(city).append("\n");
        sb.append("Teleofno: ").append(phone).append("\n");
        sb.append("Servizi: ").append(services).append("\n");
        sb.append("Punteggio: ").append(rate).append("\n");
        sb.append(ratings).append("\n");
        sb.append("Numero Recensioni: ").append(reviewCount).append("\n");
        sb.append("Rank: ").append(rank).append("\n");
        sb.append("Rank Locale: ").append(localRank);

        return sb.toString();
    }

}