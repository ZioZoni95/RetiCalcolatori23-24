package utils;

import Response_Request_netPackets.Request_ResponseMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.*;

/**
 * La classe JsonUtils fornisce metodi utili per la serializzazione e deserializzazione di oggetti in formato JSON,
 * così come per la lettura e la scrittura di file di testo. Utilizza la libreria Jackson per operazioni di
 * manipolazione JSON.
 */
public class JsonUtils {
    // Oggetto ObjectMapper di Jackson utilizzato per la serializzazione e deserializzazione JSON
    private static final ObjectMapper objectMapper;

    // Blocco statico per inizializzare l'ObjectMapper con configurazioni personalizzate
    static {
        // Crea una nuova istanza di ObjectMapper
        objectMapper = new ObjectMapper();

        // Crea un modulo personalizzato per la deserializzazione
        SimpleModule module = new SimpleModule();
        // Aggiunge un deserializzatore personalizzato per la classe Request_ResponseMessage
        module.addDeserializer(Request_ResponseMessage.class, new RequestResponeMessageDeserializer());

        // Registra il modulo nell'ObjectMapper per la deserializzazione personalizzata
        objectMapper.registerModule(module);

        // Abilita l'opzione per indentare l'output JSON (formattazione leggibile)
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Deserializza una stringa JSON in un oggetto di tipo Request_ResponseMessage.
     *
     * @param jsonData la stringa JSON da deserializzare
     * @return l'oggetto deserializzato
     * @throws IOException se si verifica un errore durante la deserializzazione
     */
    public static Request_ResponseMessage deserializeRequestResponseMessage(String jsonData) throws IOException {
        // Usa l'ObjectMapper per convertire la stringa JSON in un oggetto Request_ResponseMessage
        return objectMapper.readValue(jsonData, Request_ResponseMessage.class);
    }

    /**
     * Legge il contenuto di un file e restituisce il suo contenuto come stringa.
     * Utilizza un BufferedReader per leggere il file riga per riga e concatena le righe lette.
     *
     * @param file il file da leggere
     * @return una stringa contenente tutto il testo del file
     * @throws IOException se si verifica un errore durante la lettura del file
     */
    public static String readFile(File file) throws IOException {
        // StringBuilder per accumulare le righe lette dal file
        StringBuilder text = new StringBuilder();

        // Crea un FileReader per leggere il file e un BufferedReader per elaborare le righe
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            // Legge ogni riga del file
            while ((line = bufferedReader.readLine()) != null) {
                // Aggiunge ogni riga alla stringa di testo
                text.append(line).append(System.lineSeparator());
            }
            // Restituisce il contenuto del file come stringa
            return text.toString();
        }
    }

    /**
     * Scrive una stringa nel file specificato.
     *
     * @param text la stringa di testo da scrivere nel file
     * @param file il file su cui scrivere
     * @throws IOException se si verifica un errore durante la scrittura del file
     */
    public static void writeFile(String text, File file) throws IOException {
        // Usa un FileWriter per scrivere il contenuto nel file
        try (FileWriter writer = new FileWriter(file)) {
            // Scrive la stringa nel file
            writer.write(text);
        }
    }

    /**
     * Serializza un oggetto Java in una stringa JSON.
     * Utilizza l'ObjectMapper per convertire l'oggetto Java in un formato JSON string.
     *
     * @param obj l'oggetto Java da serializzare
     * @return una stringa contenente la rappresentazione JSON dell'oggetto
     * @throws JsonProcessingException se si verifica un errore durante la serializzazione
     */
    public static String serialize(Object obj) throws JsonProcessingException {
        // Serializza l'oggetto in formato JSON
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Deserializza una stringa JSON in un oggetto Java di tipo specificato.
     *
     * @param jsonData la stringa JSON da deserializzare
     * @param objectClass la classe dell'oggetto di destinazione
     * @param <T> il tipo dell'oggetto
     * @return l'oggetto deserializzato
     * @throws IOException se si verifica un errore durante la deserializzazione
     */
    public static <T> T deserialize(String jsonData, Class<T> objectClass) throws IOException {
        // Usa l'ObjectMapper per convertire la stringa JSON nell'oggetto di tipo specificato
        return objectMapper.readValue(jsonData, objectClass);
    }

    /**
     * Deserializza una stringa JSON in una collezione di oggetti Java.
     * Utilizza un TypeReference per gestire il tipo di collezione (ad esempio, List<Utente>).
     *
     * @param jsonData la stringa JSON da deserializzare
     * @param typeReference il tipo della collezione di oggetti di destinazione
     * @param <T> il tipo della collezione
     * @return la collezione di oggetti deserializzata
     * @throws IOException se si verifica un errore durante la deserializzazione
     */
    public static <T> T deserialize(String jsonData, TypeReference<T> typeReference) throws IOException {
        // Usa l'ObjectMapper per deserializzare la stringa JSON in una collezione di oggetti
        return objectMapper.readValue(jsonData, typeReference);
    }
}
