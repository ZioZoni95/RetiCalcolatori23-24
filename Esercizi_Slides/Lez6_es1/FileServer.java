package Lez6_es1;
import java.io.*;
import java.net.*;

/**
 *
 * Scrivere un programma JAVA che implementi un server che apre una serversocket su una porta e sta in attesa di
 * richieste di connessione.
 * Quando arriva una una richiesta di connessione, il server accetta la connessione, trasferisce al client un file e
 * poi chiude la connessione
 *  --Il server gestisce una richiesta per volta
 *  --Il server in via sempre lo stesso file, usare un file di testo
 */

class FileServer {
    public static void main (String[] args) throws Exception {
        String fileName = "home/antoninoc/RetiCalcolatori23-23/Lez6_es1/file.txt";
        //verifico che la porta viene fornita come primo comando dalla CLI
        //se nessun argomento è dato, usa la porta 6789
        int myPort = 6789;
        if (args.length > 0) {
            try {
                myPort = Integer.parseInt(args[0]);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Passare numero di porta come argomento dalla CLI");
                System.exit(-1);
            } catch (NumberFormatException e) {
                System.out.println("Il numero di porta deve essere un intero");
                System.exit(-1);
            }
        }
        //configuro socket connection
        try (ServerSocket listenSocket = new ServerSocket(myPort)) {
            //in listen per la richiesta di connessione
            System.out.println("Web server in attesa di una richiesta sulla porta " + myPort);
            while (true) {
                //set-up read and write end of the communication socket
                try (Socket connectionSocket = listenSocket.accept();
                     BufferedReader inFromClient = new BufferedReader (new InputStreamReader(connectionSocket.getInputStream()));
                     DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream())) {
                    //permette al client di richiedere un file (non funziona bene)
                    fileName = inFromClient.readLine();
                    String message = "File richiesto " + fileName;
                    outToClient.writeBytes(message);
                    //da qui funziona bene
                    File file = new File(fileName);
                    //converte il file in un array di byte
                    int numOfByte = (int) file.length();
                    try (FileInputStream inFile = new FileInputStream(fileName)) {
                        byte[] fileInBytes = new byte[numOfByte];
                        inFile.read(fileInBytes);
                        outToClient.write(fileInBytes, 0, numOfByte);
                        outToClient.flush();
                    //    connectionSocket.close(); chiude connection socket
                    } catch (FileNotFoundException e) {
                        String message1 = "File not found";
                        outToClient.writeBytes(message1);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            //    listenSocket.close(); chiude il server
            }

        }
    }
}

