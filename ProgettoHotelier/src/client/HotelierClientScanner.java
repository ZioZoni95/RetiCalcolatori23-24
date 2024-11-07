package client;

import RMI.RMIClient.HotelierClientRMI;
import client.ClientHandlers.ClientCommandHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;

import java.io.IOException;
import java.util.Arrays;

public class HotelierClientScanner implements Runnable {
    private final HotelierClientRMI clientRMI;
    private final ClientMulticast multicastReceiver;
    private final ClientCommandHandler cmd_Handler;

    public HotelierClientScanner(HotelierClientRMI clientRMI, ClientMulticast multicastReceiver) throws Exception{
        this.clientRMI = clientRMI;
        this.multicastReceiver = multicastReceiver;
        cmd_Handler = new ClientCommandHandler(clientRMI);

        Thread thread = new Thread(this);

        thread.start();
    }

    @Override
    public void run(){
        try(Scanner scanner = new Scanner(System.in)){
            System.out.println("Hotelier, i migliori hotel a portata di click! Si prega di digitare exit per uscire o help per il meù dei comandi\n");

            while (!Thread.interrupted()){
                System.out.print("Inserisci comando: ");
                String input = scanner.nextLine(); //recupero l'input da cli

                if(input.isEmpty()){
                    continue;
                }
                HotelierCommands_Client cmd = CommandParser.p_Command(input);
                if(cmd != null){
                    if(StringUtils.equalsIgnoreCase(cmd.getName(),"exit")){
                        close();
                        break;
                    }
                    try {

                        // gestisco e recupero relativa risposta del comando
                        String response = cmd_Handler.manageCommange(cmd);
                        // stampo la rispsota
                        System.out.println("\n" + response);

                        // controllo se è stata richiesta una login ed ha avuto successo
                        if (StringUtils.equals(response, "Login effettuato correttamente!")) {

                            // chiedo all' utente di inserire città di interesse per callback rmi
                            System.out.print("\nInserire le città di cui si ha particolare interesse: ");
                            // recupero input inserito dall' utente
                            input = scanner.nextLine();

                            // parso le città inserite (devono essere passate tra "") e le salvo in un array
                            String[] cities = StringUtils.substringsBetween(input, "\"", "\"");

                            // controllo se utente ha inserito almeno una città di interesse
                            if (cities != null && cities.length != 0) {

                                // registro client per callback rmi sul cambiamento del rank locale delle città di interesse
                                clientRMI.registerInterests(Arrays.asList(cities));
                                System.out.println("\nCittà di interesse registrate con successo !");
                            } else {
                                // segnalo all' utente che non si è registrato per nessuna città
                                System.out.println("\n<Attenzione> Nessuna città di interesse è stata registrata.");
                            }

                            // aggiungo client al gruppo multicast per ricevere notifiche Udp su cambiamento prima posizione del rank locale di qualsiasi città
                            multicastReceiver.joinGroup();
                        }

                        // controllo se è stata richiesta una logout ed ha avuto successo
                        if (StringUtils.equals(response, "Logout effettuato correttamente, Arrivederci!")) {

                            // richiesta logout con successo
                            // controllo se utente aveva inserito delle città di interesse per callback rmi
                            if (!clientRMI.islocalRankMapEmpty()) {

                                //deregistro client per callback rmi
                                clientRMI.removeInterest();

                                clientRMI.resetLocalRankMap();
                            }

                            //rimuovo client dal gruppo multicast per notifiche Udp
                            multicastReceiver.leaveGroup();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("");
            }
        }
    }
    private void close() {

        // deregistro client per callback rmi se utente aveva inserito città di interesse e rimuovo esportazione dello stub per le callback
        clientRMI.close();
        // chiudo la multicast socket se ancora aperta
        multicastReceiver.close();
        var tcpHandler = cmd_Handler.getHotelierClientTcpHandler();
        // chiudo socket Tcp e stream associati se socket ancora aperta
        tcpHandler.close();

    }
}
