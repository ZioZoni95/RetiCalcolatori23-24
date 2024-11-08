package HandlerMessages;

public class logoutResponseMessage extends Request_ResponseMessage {
    private String logoutResponse;

    public logoutResponseMessage(){}

    public logoutResponseMessage(String logoutResponse){
        this.logoutResponse = logoutResponse;
    }

    public String getLogoutResponse(){
        return logoutResponse;
    }
}
