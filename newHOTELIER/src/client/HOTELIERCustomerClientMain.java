package client;
import rmi.HOTELIERClientCallback;
import rmi.HOTELIERService;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class HOTELIERCustomerClientMain {
    public static void main(String[] args){
        try{
            //avvia il client RMI
            startRMIClient();

            //avvia il client TCP
            startTCPClient();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void startRMIClient() throws Exception{
        Scanner scanner = new Scanner(System.in);

        System.out.println("Avvio del client RMI...HOTELIER Service il vostro recensore di sti cazzi");
        HOTELIERClientCallback client = new HOTELIERCustomerClient();
        HOTELIERService service = (HOTELIERService) Naming.lookup("rmi://localhost:1099/HOTELIERService");

        while (true) {
            System.out.println("Seleziona un'opzione:");
            System.out.println("1. Registrati");
            System.out.println("2. Login");
            System.out.println("3. Logout");
            System.out.println("4. Esci");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Inserisci il nome utente: ");
                    String username = scanner.nextLine();
                    System.out.print("Inserisci la password: ");
                    String password = scanner.nextLine();
                  //  System.out.print("Inserisci l'email: ");
                  //  String email = scanner.nextLine();
                    //test message
                    System.out.println("sono qui");
                    System.out.println("Username: " + username);
                    System.out.println("Password: " + password);
                    System.out.println("Client Callback: " + client);
                    try {
                        String result = service.registerUser(username, password, client);
                        System.out.println("Risposta dal server: " + result);
                    } catch (RemoteException e) {
                        System.out.println("Errore durante la registrazione: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                    /*
                    String result = service.registerUser(username, password,client);
                    //test message
                    System.out.println("ho il result");
                    System.out.println("Risposta dal server: " + result);
                    break;*/
                case "2":
                    System.out.print("Inserisci il nome utente: ");
                    String loginUsername = scanner.nextLine();
                    System.out.print("Inserisci la password: ");
                    String loginPassword = scanner.nextLine();

                    String loginResult = service.logInUser(loginUsername, loginPassword, client);
                    System.out.println("Risposta dal server: " + loginResult);
                    break;

                case "3":
                    System.out.print("Inserisci il nome utente per il logout: ");
                    String logoutUsername = scanner.nextLine();
                    service.logOUTUser(logoutUsername);
                    System.out.println("Logout effettuato per " + logoutUsername);
                    break;

                case "4":
                    System.out.println("Uscita dal programma.");
                    return;

                default:
                    System.out.println("Opzione non valida.");
                    break;
                }
            }
        }
    // Metodo per avviare il client TCP
    private static void startTCPClient() {
        HotelierClientTCP tcpClient = new HotelierClientTCP();
        tcpClient.startClient();  // Avvia il client TCP
    }
}

