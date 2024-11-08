package client;

import org.apache.commons.lang3.StringUtils;

/**
 * La classe CommandParser è responsabile per il parsing dei comandi inseriti dall'utente.
 * Ogni comando è composto da un nome, un array di argomenti e un tipo (TCP, RMI, LOCAL).
 *
 * La classe esegue le seguenti operazioni:
 * 1. Parsing del nome del comando e degli argomenti.
 * 2. Controllo della validità del comando e degli argomenti.
 * 3. Creazione di un oggetto HotelierCommands_Client in base al comando e ai suoi argomenti.
 */
public class CommandParser {

    /**
     * Analizza una stringa di input per estrarre il comando e gli argomenti associati.
     *
     * @param input La stringa di input contenente il comando e gli argomenti.
     * @return Un oggetto HotelierCommands_Client che rappresenta il comando parsato.
     */
    public static HotelierCommands_Client p_Command(String input){

        String c_Name; // Nome del comando
        String[] c_args; // Argomenti del comando
        input = StringUtils.lowerCase(input).trim(); // Conversione in minuscolo e rimozione degli spazi extra
        c_Name = StringUtils.substringBefore(input, " "); // Estrazione del nome del comando prima dello spazio
        c_args = StringUtils.substringsBetween(input,"\"", "\""); // Estrazione degli argomenti tra virgolette
        HotelierCommands_Client command = commandFilter(c_Name, c_args); // Filtro del comando in base al nome e agli argomenti
        return command;
    }

    /**
     * Filtra i comandi in base al nome e agli argomenti forniti.
     *
     * @param c_name Il nome del comando.
     * @param c_args Gli argomenti del comando.
     * @return Un oggetto HotelierCommands_Client che rappresenta il comando valido, o null se non valido.
     */
    private static HotelierCommands_Client commandFilter(String c_name, String[] c_args){
        // Filtro dei comandi in base al nome
        switch (c_name) {
            case "help":
                // Il comando "help" non ha argomenti
                if(c_args == null){
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_LOCAL);
                }

            case "exit":
                // Il comando "exit" non ha argomenti
                if(c_args == null){
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_LOCAL);
                }

            case "showlocalranks":
                // Il comando "showlocalranks" non ha argomenti
                if (c_args == null) {
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_LOCAL);
                }
            case "register":
                // Il comando "register" richiede due argomenti
                if(c_args != null && c_args.length == 2){
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_RMI);
                }
                break;
            case "login":
                // Il comando "login" richiede due argomenti
                if(c_args != null && c_args.length == 2) {
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_TCP);
                }
                break;
            case "logout":
                // Il comando "logout" non ha argomenti
                if(c_args == null){
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_TCP);
                }
                break;
            case "searchhotel":
                // Il comando "searchhotel" richiede due argomenti (nome hotel e città)
                if(c_args != null && c_args.length == 2) {
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_TCP);
                }
                break;
            case "searchallhotels":
                // Il comando "searchallhotels" richiede un argomento (città)
                if(c_args != null && c_args.length == 1){
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_TCP);
                }
                break;
            case "insertreview":
                // Il comando "insertreview" richiede 7 argomenti (hotel, città, punteggi di recensione)
                if(c_args != null && checkArgsReview(c_args)){
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_TCP);
                }
                break;
            case "showmybadges":
                // Il comando "showmybadges" non ha argomenti
                if (c_args == null){
                    return new HotelierCommands_Client(c_name, c_args, HotelierCommands_Client.commandTypes.COMMAND_TCP);
                }

            default:
                // Se il comando non è riconosciuto, stampa un messaggio di errore
                System.out.println("\n Il comando " + c_name + " inserito non è supportato");
                return null;
        }
        // Se gli argomenti non sono validi o mancanti, stampa un messaggio di errore
        System.out.println("\nArgomenti invalidi/mancanti per il comando " + c_name);
        return null;
    }

    /**
     * Verifica che gli argomenti passati per il comando "insertreview" siano validi.
     * Gli argomenti devono essere 7, e i punteggi devono essere numeri interi tra 0 e 5.
     *
     * @param c_args Gli argomenti del comando "insertreview".
     * @return true se gli argomenti sono validi, false altrimenti.
     */
    private static boolean checkArgsReview(String[] c_args) {
        if (c_args.length != 7) {
            return false;
        }
        // Verifica che i punteggi siano numeri interi validi compresi tra 0 e 5
        for (int i = 2; i <= 6; i++) {
            if (!isInteger(c_args[i]) || !isValidScore(c_args[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica che un punteggio sia valido, ovvero che sia un intero compreso tra 0 e 5.
     *
     * @param score La stringa contenente il punteggio da verificare.
     * @return true se il punteggio è valido, false altrimenti.
     */
    private static boolean isValidScore (String score){

        try {
            int num = Integer.parseInt(score);
            return num >= 0 && num <= 5; // Il punteggio deve essere tra 0 e 5
        } catch (NumberFormatException e) {
            return false; // Se non è un numero intero, è invalido
        }
    }

    /**
     * Verifica se una stringa rappresenta un numero intero valido.
     *
     * @param s La stringa da verificare.
     * @return true se la stringa è un numero intero, false altrimenti.
     */
    private static boolean isInteger (String s){

        try {
            Integer.parseInt(s); // Tenta di convertire la stringa in un intero
            return true;
        } catch (NumberFormatException e) {
            return false; // Se non è un intero, restituisce false
        }
    }
}
