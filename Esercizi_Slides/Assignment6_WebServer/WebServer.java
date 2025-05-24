package Assignment6_WebServer;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Scrivere un programma JAVA che implementa un server HTTP che gestisce richieste di trasferimento di file di diverso
 * tipo (es: Immagini jpeg,gif...) provenienti da un browser web
 *      --SERVER
 *          ->sta in ascolto su una porta nota al client (es:6789)
 *          -> gestisce richieste HTTP di tipo GET alla Request URL localhost:port/filename
 *      --ULTERIORI INFORMAZIONI
 *          ->le connessioni possono essere NON persistenti
 *          ->usare le classi Socket e ServerSocket per sviluppare il programma server
 *          ->per inviare al server le richieste,utilizzare un qualsiasi browser
 */
class WebServer {
    final static int N_WORKERS = 1;
    /**
     * default port number
     */
    final static int default_port = 1024;

    public static void main(String[] args) {
        int myPort = default_port;
        if (args.length > 0) {      //controllo se la porta viene passata come argomento da CLI
            try{
                myPort = Integer.parseInt(args[0]);
            }catch (NumberFormatException e){
                System.out.println("Port number MUST be an Integer");
                System.exit(-1);
            }
        }

        //creazione del threadpool che gestisce le richieste
        ExecutorService pool = Executors.newFixedThreadPool(N_WORKERS);
        //ExecutorService pool = Executors.newCachedThreadPool();
        try(ServerSocket server = new ServerSocket(myPort)){
            System.out.printf("Web server waiting for connection on port %d\n", myPort);
            while(true){
                //rimane in listening fino a quando non riceve una richiesta
                Socket client = server.accept();
                //invio al threadpool la richiesta di gestione del client
                pool.execute(new RequestManager(client));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            //gentle-termination of threadpool
            pool.shutdown();
            try{
                while(!pool.isTerminated())
                    pool.awaitTermination(60, TimeUnit.SECONDS);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}

/**
 * Manca il request manager che gestisce le ricbhieste GET
 */
