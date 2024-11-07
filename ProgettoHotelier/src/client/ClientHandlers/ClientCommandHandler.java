package client.ClientHandlers;

import RMI.RMIClient.HotelierClientRMI;
import client.HotelierCommands_Client;

import java.io.IOException;

public class ClientCommandHandler {
    private final HotelierClientRMI clientRMI;
    private final TCPCommandHandlerClient tcpComm_Handler;
    private final rmiClientCommandHandler rmiCommHandler;

    public ClientCommandHandler(HotelierClientRMI clientRMI) throws Exception {
        this.clientRMI = clientRMI;
        tcpComm_Handler = new TCPCommandHandlerClient();
        rmiCommHandler = new rmiClientCommandHandler(clientRMI);
    }

    public String manageCommange(HotelierCommands_Client cmd) throws IOException{
        return switch(cmd.getC_type()){
            case COMMAND_TCP -> tcpComm_Handler.TCPCommandHandler(cmd);
            case COMMAND_RMI -> rmiCommHandler.handleCommand(cmd);
            case COMMAND_LOCAL -> handleLocalCMD(cmd);
            default -> null;
        };
    }

    private String handleLocalCMD(HotelierCommands_Client cmd){
        return switch (cmd.getName()){
            case "aiuto" -> handleHelpCMD(cmd);
            case "showLocalRanks" -> handleShowLocalRanks(cmd);
            default -> null;
        };
    }

    private String handleHelpCMD(HotelierCommands_Client cmd){
        StringBuilder response = new StringBuilder();
        response.append("Menù dei Comandi disponibili:\n");
        response.append("-- register \"username\" \"password\" - Registra un nuovo utente con username e password forniti\n");
        response.append("-- login \"username\" \"password\" - Effettua il login con username e password forniti\n");
        response.append("-- searchHotel \"nomeHotel\" \"città\" - Stampa hotel avente nome e città forniti\n");
        response.append("-- searchAllHotels \"città\" - Stampa tutti lista di hotel situati nella città fornita , ordinati per rank locale\n");
        response.append("-- insertReview \"nomeHotel\" \"nomeCittà\" \"GlobalScore\" \"CleaningScore\" \"PositionScore\" \"ServicesScore\" \"QualityScore\" - Inserisce una recensione per hotel avente parametri forniti\n");
        response.append("-- showMyBadges - Stampa il badge dell'utente corrispondente al livello raggiunto\n");
        response.append("-- insertReview \"nomeHotel\" \"nomeCittà\" \"GlobalScore\" \"CleaningScore\" \"PositionScore\" \"ServicesScore\" \"QualityScore\" - Inserisce una recensione per hotel avente parametri forniti\n");
        response.append("-- showLocalRanks - Stampa le liste di hotel delle città di interesse, ordinate per rank locale\n");
        response.append("-- help - Stampa la lista di comandi disponibili\n");
        response.append("-- logout - Effettua il logout\n");
        response.append("-- exit - Termina il client");

        return response.toString();
    }

    // restiusce gli hotels delle città di interesse ordinati per rank locale
    private String handleShowLocalRanks(HotelierCommands_Client cmd) {

        return clientRMI.localRankMapToString();
    }

    // restituisce istanza dell handler dei comandi tcp
    public TCPCommandHandlerClient getHotelierClientTcpHandler() {
        return tcpComm_Handler;
    }
}
