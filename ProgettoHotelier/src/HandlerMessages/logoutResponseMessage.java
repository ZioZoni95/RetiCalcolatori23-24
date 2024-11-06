package HandlerMessages;

public class logoutResponseMessage extends Request_ResponseMessage {
    private final String logoutResponse;

    public logoutResponseMessage(String logoutResponse){
        this.logoutResponse = logoutResponse;
    }

    public String getLogoutResponse(){
        return logoutResponse;
    }
}
