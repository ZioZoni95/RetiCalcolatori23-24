package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;

public class JsonUtils {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Abilita il pretty printing
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true); // Per serializzare i valori nulli
    }

    public static String readFile(File file) throws IOException {

        StringBuilder text = new StringBuilder();

        try (FileReader fileReader = new FileReader(file); BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line).append(System.lineSeparator());
            }

            return text.toString();
        }
    }

    public static void writeFile(String text, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
        }
    }

    public static String serialize(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T deserialize(String jsonData, Class<T> objectClass) throws IOException {
        return objectMapper.readValue(jsonData, objectClass);
    }
}
