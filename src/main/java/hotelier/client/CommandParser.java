package hotelier.client;

import hotelier.client.Command.*;
import hotelier.model.Ratings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Interpreta le righe digitate dall'utente: {@code comando "arg1" "arg2" ...}. */
final class CommandParser {

    private static final Pattern QUOTED = Pattern.compile("\"([^\"]*)\"\\s*");

    private CommandParser() {
    }

    static Command parse(String line) throws CommandException {
        String trimmed = line.strip();
        int split = indexOfWhitespace(trimmed);
        String name = (split < 0 ? trimmed : trimmed.substring(0, split)).toLowerCase(Locale.ROOT);
        List<String> args = quotedArgs(split < 0 ? "" : trimmed.substring(split));

        return switch (name) {
            case "help" -> noArgs(name, args, new Help());
            case "exit" -> noArgs(name, args, new Exit());
            case "showlocalranks" -> noArgs(name, args, new ShowLocalRanks());
            case "logout" -> noArgs(name, args, new Logout());
            case "showmybadges" -> noArgs(name, args, new ShowMyBadges());
            case "register" -> {
                expect(name, args, 2);
                yield new Register(args.get(0), args.get(1));
            }
            case "login" -> {
                expect(name, args, 2);
                yield new Login(args.get(0), args.get(1));
            }
            case "searchhotel" -> {
                expect(name, args, 2);
                yield new SearchHotel(args.get(0), args.get(1));
            }
            case "searchallhotels" -> {
                expect(name, args, 1);
                yield new SearchAllHotels(args.get(0));
            }
            case "insertreview" -> {
                expect(name, args, 7);
                yield new InsertReview(args.get(0), args.get(1), score(args.get(2)),
                        new Ratings(score(args.get(3)), score(args.get(4)), score(args.get(5)), score(args.get(6))));
            }
            default -> throw new CommandException("Il comando " + name + " non è supportato (digitare help)");
        };
    }

    /** Estrae gli argomenti racchiusi tra virgolette; quanto resta fuori dalle virgolette è un errore. */
    static List<String> quotedArgs(String text) throws CommandException {
        List<String> args = new ArrayList<>();
        String rest = text.strip();
        Matcher matcher = QUOTED.matcher(rest);
        int end = 0;
        while (end < rest.length() && matcher.region(end, rest.length()).lookingAt()) {
            args.add(matcher.group(1));
            end = matcher.end();
        }
        if (end < rest.length()) {
            throw new CommandException("Argomenti non validi: racchiudere ogni argomento tra virgolette");
        }
        return args;
    }

    private static Command noArgs(String name, List<String> args, Command command) throws CommandException {
        expect(name, args, 0);
        return command;
    }

    private static void expect(String name, List<String> args, int count) throws CommandException {
        if (args.size() != count) {
            throw new CommandException("Il comando " + name + " richiede " + count + " argomenti, ne sono stati forniti "
                    + args.size() + " (digitare help)");
        }
    }

    private static int score(String value) throws CommandException {
        try {
            int score = Integer.parseInt(value.strip());
            if (score >= 0 && score <= 5) {
                return score;
            }
        } catch (NumberFormatException e) {
            // gestito sotto
        }
        throw new CommandException("I punteggi devono essere numeri interi tra 0 e 5 (ricevuto \"" + value + "\")");
    }

    private static int indexOfWhitespace(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}
