package client;

public class HotelierCommands_Client {
    /**
     * 	La classe HotelierClientCommand rappresenta i comandi disponibili in Hotelier Client.
     *	Ogni comando è formato da:
     *	• un nome;
     * 	• un array di argomenti (stringhe);
     * 	• un tipo, il quale può essere:
     *		• COMMAND_TCP (gestiti da clientTcpHandler);
     *		• COMMAND_RMI (gestiti da clientRmiHandler);
     *		• COMMAND:LOCAL (gestiti da clientCommandHandler).
     */



    private final String name;
    private final String[] command_args;
    private final  commandTypes c_type;

    public HotelierCommands_Client(String name, String[] command_args, commandTypes c_type){
        this.name = name;
        this.command_args = command_args;
        this.c_type = c_type;
    }

    public String getName() {
        return name;
    }

    public String[] getCommand_args() {
        return command_args;
    }

    public commandTypes getC_type() {
        return c_type;
    }

    public enum commandTypes{
        COMMAND_TCP,
        COMMAND_RMI,
        COMMAND_LOCAL
    }
}
