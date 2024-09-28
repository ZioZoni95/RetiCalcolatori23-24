import java.io.File;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
public class StreamingTest {
	public static void main(String args[]) throws Exception{
		JsonFactory factory = new JsonFactory();
		try (JsonGenerator generator = factory.createGenerator(
				new File("output.json"), JsonEncoding.UTF8)){
			generator.setCodec(new ObjectMapper());
			
			generator.useDefaultPrettyPrinter();	
			
			generator.writeStartArray();
			// serializzo 10 oggetti persona
			Persona	p = new Persona("User", 31, "Pisa");
			for (int i = 0; i < 10; i++) {
				p.setName("User"+i);
				p.setAge(p.getAge()+i);
				generator.writeObject(p);
				System.out.println(p);
			}
			generator.writeEndArray();
			//generator.close();
		}
	}
}
