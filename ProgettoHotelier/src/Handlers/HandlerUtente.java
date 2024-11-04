package Handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.Utente;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerUtente {
    private static final String USER_DATA_FILE = "users.json"; // Percorso del file JSON
    private Map<String, Utente> users = new ConcurrentHashMap<>();
    private ObjectMapper objectMapper;

    public HandlerUtente() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        loadUsers(); // Carica gli utenti all'avvio
    }

    /**
     * Carica gli utenti dal file JSON.
     */
    private void loadUsers() {
        File file = new File(USER_DATA_FILE);
        if (!file.exists()) {
            System.out.println("File utenti non trovato, caricamento saltato.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fileChannel = fis.getChannel()) {

            // Alloca un buffer della dimensione del file
            ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
            fileChannel.read(buffer);
            buffer.flip(); // Passa il buffer in modalità lettura

            // Crea una stringa dal buffer contenente i dati JSON
            String jsonData = new String(buffer.array(), StandardCharsets.UTF_8);

            // Deserializza i dati JSON nella mappa users
            users = objectMapper.readValue(jsonData, new TypeReference<Map<String, Utente>>() {});
            System.out.println("Utenti caricati da " + USER_DATA_FILE);

        } catch (IOException e) {
            System.err.println("Errore nel caricamento degli utenti: " + e.getMessage());
        }
    }

    /**
     * Salva gli utenti nel file JSON.
     */

    public synchronized void saveUsers() {
        try (FileOutputStream fos = new FileOutputStream(USER_DATA_FILE);
             FileChannel fileChannel = fos.getChannel()) {

            // Serializza la mappa users in una stringa JSON
            String jsonData = objectMapper.writeValueAsString(users);

            // Converte la stringa JSON in un ByteBuffer
            ByteBuffer buffer = ByteBuffer.wrap(jsonData.getBytes());

            // Scrive il ByteBuffer nel FileChannel
            while (buffer.hasRemaining()) {
                fileChannel.write(buffer);
            }

            System.out.println("Utenti salvati con successo.");
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio degli utenti: " + e.getMessage());
        }
    }


    /**
     * Registra un nuovo utente, restituendo un messaggio di feedback.
     */
    public synchronized String registerUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return "Errore: Username o password vuoti";
        }
        if (users.containsKey(username)) {
            return "Errore: Username già esistente";
        }
        users.put(username, new Utente(username, password)); // Aggiunge l'utente
        saveUsers(); // Salva i dati aggiornati degli utenti
        return "Registrazione completata";
    }

    /**
     * Verifica il login di un utente, restituendo un messaggio di feedback.
     */
    public synchronized String validateUserLogin(String username, String password) {
        Utente user = users.get(username);
        if (user == null || !user.getPassword().equals(password)) {
            return "Errore: Credenziali non valide";
        }
        return "Login completato";
    }

    /**
     * Restituisce il badge dell'utente.
     */
    public synchronized String getUserBadge(String username) {
        Utente user = users.get(username);
        return user != null ? user.getBadgeLevel() : null;
    }
}
