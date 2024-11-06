package Handlers;

import RMI.RMIClient.HotelierClientRMI;
import client.HotelierCommands_Client;

import java.rmi.RemoteException;

public class rmiClientCommandHandler {
    private  final HotelierClientRMI clientRMI;

    public rmiClientCommandHandler(HotelierClientRMI clientRMI){
        this.clientRMI = clientRMI;
    }

    public String handleCommand(HotelierCommands_Client command){
        String c_name = command.getName();
        String[] c_args = command.getCommand_args();

        try{
            return switch(c_name){
                case "register" -> rmiHandleRegister(c_args);
                default -> null;
            };
        }catch (RemoteException e){
            return "Errore connessione RMI: Impossibile contattare il server RMI di Hotelier";
        }
    }

    private String rmiHandleRegister(String[] c_args) throws RemoteException{
        String username = c_args[0];
        String password = c_args[1];
        String respone = clientRMI.requesteRegisterForNewUser(username,password);
        return respone;
    }
}
