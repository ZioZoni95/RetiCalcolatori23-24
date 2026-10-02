package hotelier.client.ui;

import hotelier.model.Ratings;

/** Comando digitato dall'utente. */
sealed interface Command {

    record Help() implements Command {
    }

    record Exit() implements Command {
    }

    record ShowLocalRanks() implements Command {
    }

    record Register(String username, String password) implements Command {
    }

    record Login(String username, String password) implements Command {
    }

    record Logout() implements Command {
    }

    record SearchHotel(String hotelName, String city) implements Command {
    }

    record SearchAllHotels(String city) implements Command {
    }

    record InsertReview(String hotelName, String city, int rate, Ratings ratings) implements Command {
    }

    record ShowMyBadges() implements Command {
    }
}
