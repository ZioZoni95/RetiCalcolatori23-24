package client;

import RMI.HotelierClientRmi;

import java.util.Scanner;

public class HotelierClientMain {
    public static void main(String[] args) {
        try {
            int nioPort = 12345;
            String rmiHost = "localhost";
            int rmiPort = 1099;

            // Inizializza client RMI per register, login e logout
            HotelierClientRmi rmiClient = new HotelierClientRmi(rmiHost, rmiPort);

            // Inizializza client NIO per i comandi searchHotel e searchAllHotels
            HotelierNIOClient nioClient = new HotelierNIOClient(nioPort);

            Scanner scanner = new Scanner(System.in);
            System.out.println("Comandi disponibili: register, login, logout, searchHotel <NomeHotel> <Città>, searchAllHotels <Città>, exit");

            while (true) {
                System.out.print("Inserisci comando: ");
                String commandLine = scanner.nextLine().trim();
                String[] tokens = commandLine.split(" ");

                // Gestione dei comandi RMI
                if (tokens[0].equalsIgnoreCase("register")) {
                    if (tokens.length == 3) {
                        String response = rmiClient.register(tokens[1], tokens[2]);
                        System.out.println("Risposta RMI: " + response);
                    } else {
                        System.out.println("Comando errato. Usa: register <username> <password>");
                    }

                } else if (tokens[0].equalsIgnoreCase("login")) {
                    if (tokens.length == 3) {
                        String response = rmiClient.login(tokens[1], tokens[2]);
                        System.out.println("Risposta RMI: " + response);
                    } else {
                        System.out.println("Comando errato. Usa: login <username> <password>");
                    }

                } else if (tokens[0].equalsIgnoreCase("logout")) {
                    if (tokens.length == 2) {
                        String response = rmiClient.logout(tokens[1]);
                        System.out.println("Risposta RMI: " + response);
                    } else {
                        System.out.println("Comando errato. Usa: logout <username>");
                    }
                } else if (tokens[0].equalsIgnoreCase("exit")) {
                    System.out.println("Uscita dal client.");
                    break;

                } else {
                    System.out.println("Comando non riconosciuto.");
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
