package hotelier.client;

/** Comando non valido: il messaggio è destinato all'utente. */
final class CommandException extends Exception {

    CommandException(String message) {
        super(message);
    }
}
