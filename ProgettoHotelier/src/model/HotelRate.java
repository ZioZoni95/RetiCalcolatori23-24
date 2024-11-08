package model;

public class HotelRate {
    private float cleaning;
    private float position;
    private  float services;
    private float quality;

    public HotelRate() {}

    public HotelRate(float cleaning, float position, float services, float quality){
        this.cleaning = cleaning;
        this.position = position;
        this.services = services;
        this.quality = quality;
    }

    //costruttore di copia
    public HotelRate(HotelRate rating){
        this.cleaning = rating.cleaning;
        this.position = rating.position;
        this.services = rating.services;
        this.quality = rating.quality;
    }

    //getters and setters
    public synchronized float getCleaning(){
        return cleaning;
    }
    public synchronized void setCleaning(float cleaning){
        this.cleaning = cleaning;
    }

    public synchronized float getPosition(){
        return position;
    }
    public synchronized void setPosition(float position){
        this.position = position;
    }

    public synchronized float getServices(){
        return services;
    }
    public synchronized void setServices(float services){
        this.services = services;
    }

    public synchronized float getQuality(){
        return quality;
    }
    public synchronized void setQuality(float quality){
        this.quality = quality;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("cleaning: ").append(cleaning).append("\n");
        builder.append("position: ").append(position).append("\n");
        builder.append("services: ").append(services).append("\n");
        builder.append("quality: ").append(quality);

        return builder.toString();
    }

}