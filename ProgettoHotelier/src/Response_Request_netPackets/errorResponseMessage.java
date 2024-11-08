package Response_Request_netPackets;

public class errorResponseMessage extends Request_ResponseMessage {
    private String errorMessage;

    public errorResponseMessage(){}

    public errorResponseMessage(String errorMessage){
        this.errorMessage = errorMessage;
    }

    public String getErrorMessageResponse(){
        return errorMessage;
    }
}
