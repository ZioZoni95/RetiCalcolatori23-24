package server;

import RMI.RMIServer.HotelierServerRMI;
import server.Handlers.HotelierServerReviewManager;
import server.Handlers.HotelierServerUserManager;
import server.config.ConfigManager;
import utils.JsonUtils;

import java.io.File;
import java.io.IOException;

import static server.config.ServerJsonSettings.*;

public class HotelierServerMain {

    public static void main(String[] args) {

        try {
            // inizializzo il server
            initialize();
            System.out.println("[OK] HotelierServer inizializzato...");


            // avvio il server
            startServer();
            // stampo server avviato con successo e in esecuzione
            System.out.println("[OK] Avvio completato: HotelierServer in esecuzione e in attesa dei comandi dal client...");

        } catch (Exception e) {

            // in caso di  eccezzione nella fase di startup segnalo impossibile effettuare avvio e termino il server
            System.out.println("[ERRORE] Impossibile avviare il server. Controllare che sia già acceso !");
            e.printStackTrace();
            System.exit(0);
        }
    }

    // inizializza il server
    private static void initialize() throws IOException {

        //  controllo che esista la cartella contenente file config e strutture da persistere
        File serverFolder = new File(APPLICATION_NAME);
        // se folder non esiste lo segnalo e throwo IOException per terminare il server
        if (!serverFolder.exists()) {
            System.out.println("[ERRORE] Folder file server non trovata. Crearla al path: " + APPLICATION_NAME + " e caricare i file necessari");
            System.out.println("[ERRORE] Terminazione Server ...");
            throw new IOException();
        }

        //  controllo che esistano tutti i file richiesta per il funzionameto del server
        File hotelFile = new File(HOTELS_PATH_JSON);
        File usersFile = new File(USERS_PATH_JSON);
        File reviewsFile = new File(REVIEWS_PATH_JSON);
        File configFile = new File(SERVER_CONFIG_JSON);

        // se il file json hotel non esiste lo segnalo e throwo IOException per terminare il server
        if (!hotelFile.exists()) {
            System.out.println("[ERRORE] Hotel file json non trovato. Cariare il file nel path : " + HOTELS_PATH_JSON);
            System.out.println("[ERRORE] Terminazione Server ...");
            throw new IOException();
        }
        else {
            System.out.println("[SUCCESS] Caricamento file Hotel.json completato");
        }

        // se il file json users non esiste lo creo inzializzandola con una lista vuota
        if (!usersFile.exists()) {
            usersFile.createNewFile();
            JsonUtils.writeFile("[]", usersFile);
        }
        else {
            System.out.println("[SUCCESS] Caricamento file User.json completato");
        }

        // se il file json reviews non esiste lo creo inzializzandola con una lista vuota
        if (!reviewsFile.exists()) {
            reviewsFile.createNewFile();
            JsonUtils.writeFile("[]", reviewsFile);
        }
        else {
            System.out.println("[SUCCESS] Caricamento file Review.json completato");
        }

        // se il file json reviews non esiste lo creo inzializzandola con il file config server di default
        if (!configFile.exists()) {
            configFile.createNewFile();
            ConfigManager.createDefeaultSesttings();
        } else {
            // se esiste lo deserializzo da disco
            ConfigManager.loadServerConfig();
        }

        // ottengo istanza del registro degli hotel
        var hotelRegister = HotelierServerHotelManager.getInstance();
        // ottengo istanza del registro degli utenti
        var userRegister = HotelierServerUserManager.getInstance();
        // ottengo istanza del registro delle recensioni
        var reviewRegister = HotelierServerReviewManager.getInstance();

        // deserializzo la lista di hotel da disco all' interno del registro
        hotelRegister.deserialize();
        // deserializzo la lista di utenti da disco all' interno del registro
        userRegister.deserialize();
        // deserializzo la lista di recensioni da disco all' interno del registro
        reviewRegister.deserialize();
    }


    // avvia il server
    private static void startServer() throws Exception {

        // ottengo i server config
        var serverConfig = ConfigManager.getServerConfig();



        // inizializzo serverNIO per la gestione delle comunicazioni Tcp passandogli indirizzo e porta per la socket Tcp
        new HotelierNIOServerTCP(serverConfig.getServerAddress(), serverConfig.getTcpPort());

        // inizializzo serverRmi passandogli remoteReference per esportarzione dello stub e porta per il registro Rmi
        var hotelierServerRmi = new HotelierServerRMI(serverConfig.getRmiRemoteReference(), serverConfig.getRmiPort());

        // inizializzo sender multicast passandogli mulitcast address e porta per inviare notifiche Udp quando
        // cambia primo rank locale di qualsiasi città a tutti i client registrati al gruppo (utenti loggati)
        var hotelierServerMulticast = new HotelierServerMulticast(serverConfig.getMcastAddress(), serverConfig.getMcastPort());

        // inizializzo serverRanking passadongli rankingInterval (secondi che intercorrono tra le sue esecuzioni), serverRmi
        // e multicast
        new RankingAlgorithm(serverConfig.getRankingInterval(), hotelierServerRmi, hotelierServerMulticast);
    }

}

