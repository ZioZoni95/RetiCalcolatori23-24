package hotelier.util;

import java.io.Serializable;

/** Esito di un'operazione con messaggio destinato all'utente. */
public record Result(boolean ok, String message) implements Serializable {

    public static Result success(String message) {
        return new Result(true, message);
    }

    public static Result failure(String message) {
        return new Result(false, message);
    }
}
