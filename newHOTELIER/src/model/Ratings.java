package model;

public class Ratings {
    private int cleaning;
    private int position;
    private int services;
    private int quality;

    // Getters and Setters

    public int getCleaning() {
        return cleaning;
    }

    public void setCleaning(int cleaning) {
        this.cleaning = cleaning;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getServices() {
        return services;
    }

    public void setServices(int services) {
        this.services = services;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    @Override
    public String toString() {
        return "Ratings{" +
                "cleaning=" + cleaning +
                ", position=" + position +
                ", services=" + services +
                ", quality=" + quality +
                '}';
    }
}