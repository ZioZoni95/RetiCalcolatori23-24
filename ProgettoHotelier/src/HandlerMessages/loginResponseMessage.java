package HandlerMessages;

public class loginResponseMessage extends Request_ResponseMessage {
    private final String loginResponse;

    public loginResponseMessage(String loginResponse){
        this.loginResponse = loginResponse;
    }

    public String getLoginResponse(){
        return loginResponse;
    }
}
