package model;


import java.util.ArrayList;
import java.util.List;

public class Hotel {
    private int id;
    private final String name;
    private final String description;
    private final String city;
    private final String phone;
    private final List<String> services;
    private float rate;
    private Recensioni ratings;
    private int reviewCount;
    private double rank;
    private int localRank;

    public Hotel(int id,String name, String description, String city, String phone, List<String> services, int rate,
                Recensioni ratings){
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = new ArrayList<>(services);
        this.rate = rate;
        this.ratings = new Recensioni(ratings);
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
        this.ratings = new Recensioni(hotel.ratings);
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

    public synchronized void setRate(int rate) {
        this.rate = rate;
    }

    public synchronized Recensioni getRatings() {
        return ratings;
    }

    public synchronized void setRatings(Recensioni ratings) {
        this.ratings = ratings;
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

    public synchronized double setRankLevel(){
        this.rank = rank;
    }

    public synchronized void setLocalRanking(int localRank){
        this.localRank = localRank;
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