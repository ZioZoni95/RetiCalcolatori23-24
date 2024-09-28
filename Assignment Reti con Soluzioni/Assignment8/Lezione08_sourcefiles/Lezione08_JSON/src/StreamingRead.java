import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StreamingRead {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JsonFactory factory = new JsonFactory();
		// Create Reader/InputStream/File
		File file = new File("output.json");
		// Create JsonParser
		JsonParser parser;
		try {
			parser = factory.createParser(file);
			parser.setCodec(new ObjectMapper());
			if ( parser.nextToken() != JsonToken.START_ARRAY ) {
				//messaggio di errore
			}
			while ( parser.nextToken() == JsonToken.START_OBJECT ) {
				Persona custom = parser.readValueAs(Persona.class );
				System.out.println( "" + custom );
			}
		}
		catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
