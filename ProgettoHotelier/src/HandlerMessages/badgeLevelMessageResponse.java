package HandlerMessages;

import model.Utente;

public class badgeLevelMessageResponse extends Request_ResponseMessage {
    private final Utente.UserBadge badge;

    public badgeLevelMessageResponse(Utente.UserBadge badge){
        this.badge = badge;
    }
    public Utente.UserBadge getBadge(){
        return badge;
    }
}
