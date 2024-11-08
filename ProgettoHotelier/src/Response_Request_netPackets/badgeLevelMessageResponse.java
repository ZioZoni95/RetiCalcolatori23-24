package Response_Request_netPackets;

import model.Utente;

public class badgeLevelMessageResponse extends Request_ResponseMessage {
    private Utente.UserBadge badge;

    public badgeLevelMessageResponse(){}

    public badgeLevelMessageResponse(Utente.UserBadge badge){
        this.badge = badge;
    }
    public Utente.UserBadge getBadge(){
        return badge;
    }
}
