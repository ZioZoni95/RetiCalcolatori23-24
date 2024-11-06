package HandlerMessages;

public class searchAllHotelsMessage extends Request_ResponseMessage {
    private final String city;

    public searchAllHotelsMessage(String city){
        this.city = city;
    }
    public String getCity(){
        return city;
    }
}
