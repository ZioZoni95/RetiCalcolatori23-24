package Response_Request_netPackets;

public class errorResponseMessage extends Request_ResponseMessage {
  /*  @JsonIgnoreProperties(ignoreUnknown = true)*/
    private String errorMessageResponse;

    public errorResponseMessage(){}

    public errorResponseMessage(String errorMessageResponse){
        this.errorMessageResponse = errorMessageResponse;
    }

    public String getErrorMessageResponse(){
        return errorMessageResponse;
    }
}
