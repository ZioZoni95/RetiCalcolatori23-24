package Response_Request_netPackets;

public class loginMessageRequest extends Request_ResponseMessage {
    private  String username;
    private  String password;

    public loginMessageRequest() {}
    public loginMessageRequest(String username, String password){
        this.username = username;
        this.password = password;
    }

    //public loginMessageRequest(){}

    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }
}
