package Response_Request_netPackets;

public class searchAllHotelsMessage extends Request_ResponseMessage {
    private String city;

    public searchAllHotelsMessage() {}

    public searchAllHotelsMessage(String city){
        this.city = city;
    }
    public String getCity(){
        return city;
    }
}
