package Response_Request_netPackets;

public class loginResponseMessage extends Request_ResponseMessage {
    private  String loginResponse;

    public loginResponseMessage() {}
    public loginResponseMessage(String loginResponse){
        this.loginResponse = loginResponse;
    }

    public String getLoginResponse(){
        return loginResponse;
    }
}
