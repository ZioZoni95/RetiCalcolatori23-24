package HandlerMessages;

public class loginMessageRequest extends Request_ResponseMessage {
    private final String username;
    private final String password;

    public loginMessageRequest(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }
}
