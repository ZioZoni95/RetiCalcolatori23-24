package hotelier.client.ui;

import hotelier.client.ui.Command.*;
import hotelier.model.Ratings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandParserTest {

    @Test
    void passwordsKeepTheirCase() throws CommandException {
        assertEquals(new Login("Mario", "PaSs"), CommandParser.parse("LOGIN \"Mario\" \"PaSs\""));
    }

    @Test
    void commandNamesAreCaseInsensitive() throws CommandException {
        assertEquals(new SearchAllHotels("Roma"), CommandParser.parse("searchAllHotels \"Roma\""));
        assertEquals(new ShowMyBadges(), CommandParser.parse("  showMyBadges  "));
    }

    @Test
    void argumentsMayContainSpaces() throws CommandException {
        assertEquals(new SearchHotel("Hotel Aosta 1", "Aosta"),
                CommandParser.parse("searchHotel \"Hotel Aosta 1\" \"Aosta\""));
    }

    @Test
    void parsesAReview() throws CommandException {
        assertEquals(new InsertReview("H", "Roma", 4, new Ratings(1, 2, 3, 5)),
                CommandParser.parse("insertReview \"H\" \"Roma\" \"4\" \"1\" \"2\" \"3\" \"5\""));
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(CommandException.class, () -> CommandParser.parse("help extra"));
        assertThrows(CommandException.class, () -> CommandParser.parse("help \"extra\""));
        assertThrows(CommandException.class, () -> CommandParser.parse("login \"solo\""));
        assertThrows(CommandException.class, () -> CommandParser.parse("login mario pw"));
        assertThrows(CommandException.class, () -> CommandParser.parse("foo"));
        assertThrows(CommandException.class,
                () -> CommandParser.parse("insertReview \"H\" \"Roma\" \"6\" \"1\" \"2\" \"3\" \"5\""));
        assertThrows(CommandException.class,
                () -> CommandParser.parse("insertReview \"H\" \"Roma\" \"x\" \"1\" \"2\" \"3\" \"5\""));
    }

    @Test
    void quotedArgs() throws CommandException {
        assertEquals(List.of("Roma", "Milano"), CommandParser.quotedArgs(" \"Roma\"   \"Milano\" "));
        assertEquals(List.of(), CommandParser.quotedArgs("   "));
        assertThrows(CommandException.class, () -> CommandParser.quotedArgs("\"Roma\" Milano"));
    }
}
