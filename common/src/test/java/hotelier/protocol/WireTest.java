package hotelier.protocol;

import hotelier.model.Badge;
import hotelier.model.Hotel;
import hotelier.model.Ratings;
import hotelier.protocol.Message.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WireTest {

    private static final Hotel HOTEL =
            new Hotel(7, "Hotel Roma 7", "descr", "Roma", "123", List.of("TV", "Wifi"), 4.5,
                    new Ratings(4, 5, 4, 5), 2, 3.9, 1);

    @Test
    void everyMessageSurvivesARoundTrip() throws IOException {
        List<Message> messages = List.of(
                new LoginRequest("mario", "pw"),
                new LogoutRequest(),
                new SearchHotelRequest("Hotel Roma 7", "Roma"),
                new SearchCityRequest("Roma"),
                new InsertReviewRequest("Hotel Roma 7", "Roma", 4, new Ratings(1, 2, 3, 4)),
                new BadgeRequest(),
                new Success("ok"),
                new Failure("no"),
                new HotelResult(HOTEL),
                new HotelListResult(List.of(HOTEL, HOTEL)),
                new BadgeResult(Badge.EXPERT_CONTRIBUTOR));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Message message : messages) {
            Wire.write(out, message);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        for (Message expected : messages) {
            assertEquals(expected, Wire.read(in));
        }
    }

    @Test
    void messageTypeIsAnExplicitField() throws IOException {
        byte[] frame = Wire.encode(new SearchCityRequest("Roma"));
        String json = new String(frame, 4, frame.length - 4);
        assertEquals("{\"type\":\"search_city\",\"city\":\"Roma\"}", json);
    }

    @Test
    void oversizedFramesAreRejected() {
        byte[] header = ByteBuffer.allocate(4).putInt(Wire.MAX_FRAME_SIZE + 1).array();
        assertThrows(IOException.class, () -> Wire.read(new ByteArrayInputStream(header)));
    }
}
