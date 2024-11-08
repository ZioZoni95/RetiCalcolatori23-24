package utils;

import Response_Request_netPackets.Request_ResponseMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.*;

public class JsonUtils {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Request_ResponseMessage.class, new RequestResponeMessageDeserializer());
        objectMapper.registerModule(module);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static Request_ResponseMessage deserializeRequestResponseMessage(String jsonData) throws IOException {
        return objectMapper.readValue(jsonData, Request_ResponseMessage.class);
    }

    public static String readFile(File file) throws IOException {
        StringBuilder text = new StringBuilder();
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
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

    // Serialize an object to JSON string
    public static String serialize(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    // Deserialize JSON string to a single object
    public static <T> T deserialize(String jsonData, Class<T> objectClass) throws IOException {
        return objectMapper.readValue(jsonData, objectClass);
    }

    // Deserialize JSON string to a collection (e.g., List<Utente>)
    public static <T> T deserialize(String jsonData, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(jsonData, typeReference);
    }
}
