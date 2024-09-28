
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;  

public class JSONFileCreator {  

	public static void main(String[] args) {  
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		Country countryObj = new Country();  
		countryObj.setName("Italia"); 
		countryObj.setPopulation(54000000);  
		countryObj.addRegion("Toscana");  
		countryObj.addRegion("Sicilia");  
		countryObj.addRegion("Veneto");  
		try {   
			// Writing to a file  
			File file=new File("RegionFileJackson.json");  
		
			System.out.println("Writing JSON object to file"); 
			
			System.out.println("-----------------------");  
			objectMapper.writeValue(file, countryObj);
			System.out.println("Writing JSON object to string");  
			System.out.println(objectMapper.writeValueAsString(countryObj));
		//	FileWriter fileWriter = new FileWriter(file);  
		//	objectMapper.writeValue(fileWriter, countryObj);
		//	fileWriter.close();  
		} 
		catch (IOException e) {  
			e.printStackTrace();  
		}  
		
	}
}  
