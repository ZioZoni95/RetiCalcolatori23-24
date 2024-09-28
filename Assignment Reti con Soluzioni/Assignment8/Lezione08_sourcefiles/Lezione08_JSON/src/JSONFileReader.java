
import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;  

public class JSONFileReader {  

	public static void main(String[] args) {  
		ObjectMapper objectMapper = new ObjectMapper();
		File file=new File("RegionFileJackson.json"); 
		Country newCountry;
		try {
			newCountry = objectMapper.readValue(file, Country.class);
			System.out.println("Deserialized object from JSON");  
			System.out.println("-----------------------");  
			System.out.println("Country name " + newCountry.getName() + " Population " + newCountry.getPopulation()); 
			System.out.println("Country regions " + newCountry.getRegions()); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}  
}