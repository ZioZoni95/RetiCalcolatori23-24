package client;

/**
 * La classe HotelierCommands_Client rappresenta i comandi disponibili nel client Hotelier.
 * Ogni comando è costituito da tre componenti principali:
 * - un nome (stringa);
 * - un array di argomenti (stringhe) che vengono forniti insieme al comando;
 * - un tipo che indica il tipo di gestione del comando (TCP, RMI, o Locale).
 *
 * I comandi possono essere gestiti in modo diverso a seconda del tipo:
 * - **COMMAND_TCP**: Gestito dal `TCPCommandClientHandler` per le comunicazioni TCP.
 * - **COMMAND_RMI**: Gestito dal `rmiClientCommandHandler` per le chiamate RMI (Remote Method Invocation).
 * - **COMMAND_LOCAL**: Gestito dal `ClientCommandHandler` per operazioni locali.
 */
public class HotelierCommands_Client {

    private final String name;  // Nome del comando (es. "login", "logout", ecc.)
    private final String[] command_args;  // Array contenente gli argomenti del comando (es. città, username, ecc.)
    private final commandTypes c_type;  // Tipo del comando (TCP, RMI, Locale)

    /**
     * Costruttore della classe HotelierCommands_Client.
     *
     * @param name Nome del comando.
     * @param command_args Array di argomenti per il comando.
     * @param c_type Tipo di comando (TCP, RMI, Locale).
     */
    public HotelierCommands_Client(String name, String[] command_args, commandTypes c_type){
        this.name = name;
        this.command_args = command_args;
        this.c_type = c_type;
    }

    /**
     * Metodo per ottenere il nome del comando.
     *
     * @return Il nome del comando.
     */
    public String getName() {
        return name;
    }

    /**
     * Metodo per ottenere gli argomenti del comando.
     *
     * @return L'array di argomenti del comando.
     */
    public String[] getCommand_args() {
        return command_args;
    }

    /**
     * Metodo per ottenere il tipo del comando.
     *
     * @return Il tipo del comando (TCP, RMI, Locale).
     */
    public commandTypes getC_type() {
        return c_type;
    }

    /**
     * Enumerazione che definisce i tipi di comando disponibili:
     * - **COMMAND_TCP**: Comandi gestiti tramite TCP.
     * - **COMMAND_RMI**: Comandi gestiti tramite RMI.
     * - **COMMAND_LOCAL**: Comandi gestiti localmente, senza bisogno di comunicazione di rete.
     */
    public enum commandTypes {
        COMMAND_TCP,   // Tipo di comando TCP
        COMMAND_RMI,   // Tipo di comando RMI
        COMMAND_LOCAL  // Tipo di comando Locale
    }
}
