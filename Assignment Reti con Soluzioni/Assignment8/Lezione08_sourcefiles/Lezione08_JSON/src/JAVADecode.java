import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;  

public class JAVADecode {
	public static void main(String[] args){
		String s="{\"book\": [{\"id\": "
				+ "1,\"title\":\"Il Nome della Rosa\","
				+ "\"author\": \"Umberto Eco\"}, "
				+ "{\"id\": 2, \"title\": \"I Promessi Sposi\","
				+ "\"author\": \"Alessandro Manzoni\"}]}";
		ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			JsonNode  arrNode = objectMapper.readTree(s).get("book");	
			if (arrNode.isArray()) {
				for (JsonNode objNode : arrNode) {
					System.out.println(objNode.toString());			
				}
			}
			//System.out.println(objectMapper.writeValueAsString(arrNode));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}