package hotelier.server;

/** I servizi condivisi da tutte le connessioni. */
public record Services(UserService users, HotelRepository hotels, ReviewService reviews,
                       SessionRegistry sessions, EventLog events) {
}
