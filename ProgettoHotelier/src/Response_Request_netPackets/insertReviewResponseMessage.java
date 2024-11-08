package Response_Request_netPackets;

public class insertReviewResponseMessage extends Request_ResponseMessage {

    private String responseMessage;

    public insertReviewResponseMessage(){}

    public insertReviewResponseMessage(String responseMessage){
        this.responseMessage = responseMessage;
    }
    public String getResponseMessage(){
        return responseMessage;
    }
}
