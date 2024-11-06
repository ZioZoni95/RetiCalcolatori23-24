package model;

public class HotelRate {
    private float cleaning;
    private float position;
    private  float services;
    private float quality;

    public HotelRate(float cleaning, float position, float services, float quality){
        this.cleaning = cleaning;
        this.position = position;
        this.services = services;
        this.quality = quality;
    }

    //costruttore di copia
    public HotelRate(HotelRate rating){
        this.cleaning = cleaning;
        this.position = position;
        this.services = services;
        this.quality = quality;
    }

    //getters and setters
    public synchronized float getCleaning(){
        return cleaning;
    }
    public synchronized void setCleaning(){
        this.cleaning = cleaning;
    }

    public synchronized float getPosition(){
        return position;
    }
    public synchronized void setPosition(){
        this.position = position;
    }

    public synchronized float getServices(){
        return services;
    }
    public synchronized void setServices(){
        this.services = services;
    }

    public synchronized float getQuality(){
        return quality;
    }
    public synchronized void setQuality(){
        this.quality = quality;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Pulizia: ").append(cleaning).append("\n");
        builder.append("Posizione: ").append(position).append("\n");
        builder.append("Servizi: ").append(services).append("\n");
        builder.append("Qualità: ").append(quality);

        return builder.toString();
    }

}