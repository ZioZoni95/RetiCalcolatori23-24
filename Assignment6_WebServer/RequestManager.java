package Assignment6_WebServer;
import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.StringTokenizer;

/**
 * RequestManager modella il gestore di una singola richiesta del client
 * L'unico tipo di richiesta accetta è una richiesta GET
 *
 * Mood song: Breathe(in the air)
 */

public class RequestManager implements Runnable {
    /**
     * Campi HTTP del metodo GET
     */
    private static final String VERSION = "HTTP/1.0";
    private static final String STATUS_SUCCESS = "200 OK";
    private static final String STATUS_NOT_FOUND = "404 Not Found";
    private static final String MESSAGE_ERROR = "File not found";
    private static final String CONTENT_TYPE = "Content-Type: ";
    private static final String CONTENT_LENGHT = "Content-Lenght: ";
    private static final String CR = "\r\n";
    private final Socket client;

    /**
     *
     * @param client connection socket utilizzata per la comunicazione con il client
     */
    public RequestManager(Socket client){
        this.client = client;
    }

    @Override
    public void run(){
        //associa gli stream di input (read) e output (write) al socket
        try(BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());){

            //recover the first request line to get the requested "method"
            String requestMessageLine = inFromClient.readLine();
            if(requestMessageLine == null){
                System.out.println("Null message");
                client.close();
                return;
            }

            StringTokenizer tokeniedLine = new StringTokenizer(requestMessageLine);

            //print client request
            System.out.println("#####REQUEST INCOMING####");
            System.out.println("Request:\n" + requestMessageLine);

            while(requestMessageLine.length() >= 3){
                System.out.println(requestMessageLine);
                requestMessageLine = inFromClient.readLine();
            }
            System.out.println("#########################");

            if(tokeniedLine.nextToken().equals("GET")){
                String filename = tokeniedLine.nextToken();
                //remove separatore iniziale if exist
                if(filename.startsWith("/"))
                    filename = filename.substring(1);
                //accedi al file richiesto
                File file = new File(filename);

                StringBuilder response = new StringBuilder();
                byte[] messageResponse;

                if (file.exists()){     //se il file richiesto esiste allora ok e procedi
                    int numOfBytes = (int) file.length();
                    response.append(VERSION).append(STATUS_SUCCESS).append(CR);
                    String mimeType = URLConnection.guessContentTypeFromName(file.getName());
                    if(mimeType == null){
                        response.append(CONTENT_TYPE).append("text/plain").append(CR);
                    }
                    else{
                        response.append(CONTENT_TYPE).append(mimeType).append(CR);
                    }
                    response.append(CONTENT_LENGHT).append(numOfBytes).append(CR);
                    FileInputStream inFile = new FileInputStream(filename);
                    messageResponse = new byte[numOfBytes];
                    inFile.read(messageResponse);
                    inFile.close();
                }
                else{
                    response.append(VERSION).append(STATUS_NOT_FOUND).append(CR);
                    response.append(CONTENT_TYPE).append("text/plain").append(CR);
                    response.append(CONTENT_LENGHT).append(MESSAGE_ERROR.length()).append(CR);
                    messageResponse = MESSAGE_ERROR.getBytes();
                }
                response.append(CR);

                //invia al client la risposta del server
                outToClient.writeBytes(response.toString());
                outToClient.write(messageResponse, 0, messageResponse.length);

                //stampa la risposta del server
                System.out.println("----------------------------");
                System.out.printf("Response:\n%s%s\n",response.toString(), new String(messageResponse));
                System.out.println("----------------------------");
            }
            else{
                System.out.println("Bad Request message");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            //chiude il socket del client
            try{
                client.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
