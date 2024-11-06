package HandlerMessages;

public class errorResponseMessage extends Request_ResponseMessage {
    private final String errorMessage;

    public errorResponseMessage(String errorMessage){
        this.errorMessage = errorMessage;
    }

    public String getErrorMessageResponse(){
        return errorMessage;
    }
}
