package client;

import RMI.RMIClient.HotelierClientRMI;
import client.config.ClientConfigManager;

import java.io.File;
import java.io.IOException;

import static client.config.ClientConfigSetting.CLIENT_CONFIG_PATH_JSON;

public class HotelierClientMain {


    public static void main(String[] args) throws IOException  {

        // inizializzo il server
        initialize();
        // avvio il client
        startCLient();
    }

    // inizializza il server
    private static void initialize() throws IOException {

        // recupero il file config del client dal path specificato
        File configFile = new File(CLIENT_CONFIG_PATH_JSON);

        // controllo se il file config esiste
        if (!configFile.exists()) {
            // file non esiste: creo un nuovo file config con path specificato e valori di defult
            configFile.createNewFile();
            ClientConfigManager.createDeafaultSettings();
        } else {
            // file esiste: deserializzo il file da disco
            ClientConfigManager.loadConfigCLient();
        }
    }

    // avvio il client
    private static void startCLient() {

        try {
            // ottengo i config del client
            var clientConfig = ClientConfigManager.getClientConfig();
            // inizializzo client rmi passando server address e rmiRemoteReference per recuperare stub server
            var clientRmi = new HotelierClientRMI(clientConfig.getServerAddress(), clientConfig.getRmiRemoteReference(), clientConfig.getRmiPort());
            // inizializzo reciever multicast passando mulitcast address e porta per ricevere notifiche Udp quando cambia primo hotel di qualsiasi rank locale
            var multicastReciever = new ClientMulticast(clientConfig.getMcastAddress(), clientConfig.getMcastPort());
            // inizializzo client cli passando client rmi e multicast reciever per la gestione dei comandi/richieste dell'utente
            var clientScanner = new HotelierClientScanner(clientRmi, multicastReciever);

        } catch (Exception e) {

            //In caso di eccezione nella fase di startUp del client segnalo che è impossibile contattare il server
            System.out.println("<Errore> Impossibile contattare il server di Hotelier.");
            System.out.println("Terminazione Hotelier client ...");
        }
    }

}
