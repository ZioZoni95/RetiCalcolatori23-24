package client;

import RMI.HotelierClientRmiImp;

import java.util.Scanner;

public class HotelierClientMain {
    public static void main(String[] args) {
        try {
            int nioPort = 9999;
            String rmiHost = "localhost";
            int rmiPort = 1099;

            // Inizializza client RMI per register, login e logout
            HotelierClientRmiImp rmiClient = new HotelierClientRmiImp(rmiHost, rmiPort);

            // Inizializza client NIO per i comandi searchHotel e searchAllHotels
            HotelierNIOClient nioClient = new HotelierNIOClient(nioPort);

            // Start the NIO client in a new thread
            Thread nioClientThread = new Thread(nioClient);
            nioClientThread.start();

            Scanner scanner = new Scanner(System.in);
            System.out.println("Comandi disponibili: register, login, logout, searchHotel <NomeHotel> <Città>, searchAllHotels <Città>, exit");

            while (true) {
                System.out.print("Inserisci comando: ");
                String commandLine = scanner.nextLine().trim();
                String[] tokens = commandLine.split(" ");

                // Check if there are any tokens
                if (tokens.length == 0) {
                    System.out.println("Comando non riconosciuto.");
                    continue;
                }

                // Use switch-case to handle commands
                switch (tokens[0].toLowerCase()) {
                    case "register":
                        if (tokens.length == 3) {
                            String response = rmiClient.register(tokens[1], tokens[2]);
                            System.out.println("Risposta RMI: " + response);
                        } else {
                            System.out.println("Comando errato. Usa: register <username> <password>");
                        }
                        break;

                    case "login":
                        if (tokens.length == 3) {
                            String response = rmiClient.login(tokens[1], tokens[2]);
                            System.out.println("Risposta RMI: " + response);
                        } else {
                            System.out.println("Comando errato. Usa: login <username> <password>");
                        }
                        break;

                    case "logout":
                        if (tokens.length == 2) {
                            String response = rmiClient.logout(tokens[1]);
                            System.out.println("Risposta RMI: " + response);
                        } else {
                            System.out.println("Comando errato. Usa: logout <username>");
                        }
                        break;

                    case "searchhotel":
                    case "searchallhotels":
                        // Invia comando al client NIO
                        nioClient.sendCommand(commandLine);
                        break;

                    case "exit":
                        System.out.println("Uscita dal client.");
                        scanner.close(); // Ensure the scanner is closed before breaking
                        return; // Exit the main method

                    default:
                        System.out.println("Comando non riconosciuto.");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

