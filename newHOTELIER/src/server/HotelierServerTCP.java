package server;
import model.Utente;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;


public class HotelierServerTCP {
    private static final int PORT = 8081;
    private ExecutorService threadpool;
    private List<Utente> users;

    public HotelierServerTCP(){
        //inizializzo il threadpool
        threadpool = Executors.newFixedThreadPool(10);
        //carica utenti da json
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket();) {
            InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8081);
            serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), PORT));
            // System.out.println("Server TCP avviato sulla porta " + PORT);
            while (true) {
                System.out.println("Server TCP avviato sulla porta " + PORT);
                Socket clientSocket = serverSocket.accept();
                threadpool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try(DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));) {

            String command = inputStream.readLine();
            while (command != null) {
                // legge il comando inviato dal client
                System.out.println("Comando ricevuto: " + command);
                if (command.startsWith("login")) {
                    handleLogin(command, writer);
                } else if (command.startsWith("Registrazione")) {
                    handleRegister(command, writer);
                } else {
                    System.out.println("Comando non riconosciuto: " + command);
                }
                writer.write(command + "\r\n");
                writer.flush();
            }
        }catch (IOException e) {
         System.out.println("Client closed or error");
            } finally {
                try {
                    clientSocket.close();  // Chiude la connessione
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         *
         *
         */
    private void handleRegister(String command, BufferedWriter out) {
        String[] parts = command.split(" ");

        if (parts.length == 3) {  // Deve avere username, password, email
            String username = parts[1];
            String password = parts[2];
            // Verifica se l'username esiste già
            for (Utente user : users) {
                if (user.getUsername().equals(username)) {
                    System.out.println("Errore: Nome utente già in uso.");
                    System.out.println("Nome utente già in uso: " + username);
                    return;
                }
            }
            // Aggiungi il nuovo utente alla lista
            Utente newUser = new Utente(username, password);
            users.add(newUser);

            // Salva gli utenti aggiornati nel file JSON
            //JsonUtils.saveUsersToFile(users, "resources/users.json");

            System.out.println("Registrazione avvenuta con successo per l'utente " + username);
            System.out.println("Registrazione completata per l'utente: " + username);
        } else {
            // Comando di registrazione errato
            System.out.println("Formato del comando register non corretto.");
            System.out.println("Formato del comando register non corretto: " + command);
        }
    }

    private void handleLogin(String command, BufferedWriter out) {
        String[] parts = command.split(" ");

        if (parts.length == 3) {  // Deve avere username e password
            String username = parts[1];
            String password = parts[2];

            // Cerca l'utente nella lista degli utenti
            for (Utente user : users) {
                if (user.getUsername().equals(username)) {  // Utente trovato
                    if (user.checkPassword(password)) {  // Password corretta
                        System.out.println("Login avvenuto con successo per l'utente " + username);
                        System.out.println("Login avvenuto per l'utente: " + username);
                        return;
                    } else {  // Password errata
                        System.out.println("Login fallito. Password errata.");
                        System.out.println("Password errata per l'utente: " + username);
                        return;
                    }
                }
            }
            // Utente non trovato
            System.out.println("Login fallito. Utente non trovato.");
            System.out.println("Utente non trovato: " + username);
        } else {
            // Comando di login errato
            System.out.println("Formato del comando login non corretto.");
            System.out.println("Formato del comando login non corretto: " + command);
        }
    }
}