package client;

import org.apache.commons.lang3.StringUtils;
public class CommandParser {
    /**
     * La classe HotelierClientCommandParser è una classe statica con il compito di parsare i comandi dall' input inserito dall' utente.
     * Ogni comando è formato da: un nome, un array di argomenti (stringhe) e un tipo (TCP, RMI, LOCAL), ottenuti come segue:
     * • nome: stringa prima del primo spazio in input;
     * • argomenti: stringhe contenute tra virgolette ("argomento");
     * • tipo: una volta controllata la validità degli argomenti viene assegnato in base al nome del comando.
     */

    public static HotelierCommands_Client p_Command(String input){

        String c_Name; //nome del comando
        String[] c_args;//argomenti comando passato
        input = StringUtils.lowerCase(input).trim(); // conversione a lower case per poi eseguire il trim
        c_Name = StringUtils.substringBefore(input, " "); //parsing del comando
        c_args = StringUtils.substringsBetween(input,"\"", "\""); // parso gli argomenti del comando (argomenti devono essere passati tra "")
        HotelierCommands_Client command = commandFilter(c_Name,c_args); //filtro i comandi
        return command;
    }

    private static HotelierCommands_Client commandFilter(String c_name, String[] c_args){
        //filtro per nome del comando
        switch (c_name) {
            case "exit":
                if(c_args == null){
                    return  new HotelierCommands_Client(c_name,c_args, HotelierCommands_Client.commandTypes.COMMAND_LOCAL);
                }
            case "showlocalranks":
                // controllo che non siano stati passati argomenti
                if (c_args == null) {
                    // restituisco comando showlocalranks
                    return new HotelierCommands_Client(c_name,c_args, HotelierCommands_Client.commandTypes.COMMAND_LOCAL);
                    }
            case "register":
                if(c_args !=null && c_args.length == 2){
                    return new HotelierCommands_Client(c_name,c_args, HotelierCommands_Client.commandTypes.COMMAND_RMI);
                }
                break;
            case "login":
                if(c_args !=null && c_args.length ==2) {
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_RMI);
                }
                break;
            case "logout":
                if(c_args == null){
                    return new HotelierCommands_Client(c_name,c_args, HotelierCommands_Client.commandTypes.COMMAND_RMI);
                }
                break;
            case "searchHotel":
                if(c_args != null && c_args.length == 2) {
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_TCP);
                }
                break;
            case "searchAllHotels":
                if(c_args != null && c_args.length == 1){
                    return new HotelierCommands_Client(c_name,c_args, HotelierCommands_Client.commandTypes.COMMAND_TCP);
                }
                break;
            case "insertReview":
                if(c_args != null && checkArgsReview(c_args)){
                    return new HotelierCommands_Client(c_name,c_args, HotelierCommands_Client.commandTypes.COMMAND_TCP);
                }
                break;
            case "showMyBadges":
                if (c_args == null){
                    return new HotelierCommands_Client(c_name,c_args, HotelierCommands_Client.commandTypes.COMMAND_TCP);
                }

            default:
                System.out.println("\n Il comando " + c_name + " inserito non è supportato");
                return null;
        }
        // segnalo argomenti invalidi/mancanti per il comando e restituisco null
        System.out.println("\nArgomenti invalidi/mancanti per il comando " + c_name);
        return null;
    }

    private static boolean checkArgsReview(String[] c_args) {
        if (c_args.length != 7) {
            return false;
        }
        for (int i = 0; i <= 6; i++) {
            if (!isInteger(c_args[i]) || isValidScore(c_args[i])) {
                return false;
            }
        }
        return true;
    }

        // restituisce true se punteggio valido (intero e compreso tra 0 e 5), false altrimenti
        private static boolean isValidScore (String score){

            try {
                int num = Integer.parseInt(score);
                return num >= 0 && num <= 5;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // restisce true se la stringa contine un itero, false altrimenti
        private static boolean isInteger (String s){

            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

