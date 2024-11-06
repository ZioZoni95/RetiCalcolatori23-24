package HandlerMessages;

public class insertReviewResponseMessage extends Request_ResponseMessage {

    private final String responseMessage;

    public insertReviewResponseMessage(String responseMessage){
        this.responseMessage = responseMessage;
    }
    public String getResponseMessage(){
        return responseMessage;
    }
}
