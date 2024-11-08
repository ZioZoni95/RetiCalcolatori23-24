package server.Handlers;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import Response_Request_netPackets.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * La classe `HotelierServerConnectionClientHandler` gestisce la connessione tra il server e un client
 * per la gestione dei pacchetti di richiesta e risposta in un'applicazione client-server.
 * Gestisce la lettura dei dati inviati dal client, la gestione dei pacchetti, e la scrittura delle risposte.
 */
public class HotelierServerConnectionClientHandler {

    // Canale di comunicazione associato al client per la lettura e la scrittura dei dati
    private final SocketChannel client;

    // Gestore dei pacchetti di messaggi
    private final HotelierServerMessageManager hotelierMessageManager;

    // Coda di pacchetti di risposta da inviare al client
    private final Queue<Request_ResponseMessage> responseQueue;

    // Buffer per leggere l'intestazione della richiesta (header) e il payload
    private ByteBuffer requestHeader, requestPayload;

    // Buffer per la scrittura delle risposte
    private ByteBuffer responseBuffer;

    // Stato della connessione con il client (true = connesso, false = disconnesso)
    private boolean isConnected;

    // Jackson ObjectMapper per la serializzazione e deserializzazione in formato JSON
    private final ObjectMapper objectMapper;

    /**
     * Costruttore della classe. Inizializza il canale del client, il gestore dei messaggi,
     * la coda delle risposte, e l'ObjectMapper per la serializzazione/deserializzazione.
     *
     * @param client Il canale SocketChannel associato al client
     */
    public HotelierServerConnectionClientHandler(SocketChannel client) {
        this.client = client;
        hotelierMessageManager = new HotelierServerMessageManager(); // Gestore dei messaggi
        responseQueue = new LinkedList<>(); // Coda per le risposte
        isConnected = true; // Il client è connesso all'inizio

        // Inizializzazione dell'ObjectMapper per JSON
        objectMapper = new ObjectMapper();
    }

    /**
     * Gestisce la lettura dei pacchetti di richiesta inviati dal client.
     * Legge prima l'intestazione (header) e successivamente il payload del pacchetto.
     * Una volta letti i dati, deserializza il pacchetto e restituisce l'oggetto corrispondente.
     *
     * @return Il pacchetto deserializzato o null se la connessione è chiusa.
     */
    public Request_ResponseMessage handleRead() {
        if (isConnected) {
            try {
                // Se non è stato ancora allocato, inizializza il buffer dell'intestazione (header)
                if (requestHeader == null) {
                    requestHeader = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
                }

                // Legge l'intestazione se ci sono ancora dati da leggere
                if (requestHeader.hasRemaining()) {
                    if (client.read(requestHeader) == -1) {
                        throw new IOException();
                    }
                }

                // Una volta letta l'intestazione, inizializza il buffer del payload
                if (!requestHeader.hasRemaining() && requestPayload == null) {
                    requestHeader.flip();
                    int payloadSize = requestHeader.getInt(); // Ottieni la dimensione del payload
                    requestPayload = ByteBuffer.allocate(payloadSize).order(ByteOrder.BIG_ENDIAN); // Alloca il buffer per il payload
                }

                // Legge il payload se ci sono ancora dati da leggere
                if (requestPayload != null && requestPayload.hasRemaining()) {
                    if (client.read(requestPayload) == -1) {
                        throw new IOException("<Errore di lettura>: client disconnesso durante la lettura del payload");
                    }
                }

                // Una volta letto tutto il payload, deserializza il pacchetto
                if (requestPayload != null && !requestPayload.hasRemaining()) {
                    int packetID = requestHeader.getInt(); // Ottieni l'ID del pacchetto
                    requestPayload.flip();
                    Request_ResponseMessage packet = deserializeRequest(packetID, requestPayload); // Deserializza il pacchetto
                    requestHeader = null; // Resetta il buffer dell'intestazione
                    requestPayload = null; // Resetta il buffer del payload
                    return packet; // Restituisce il pacchetto deserializzato
                }
            } catch (IOException exception) {
                isConnected = false; // Imposta la connessione come chiusa in caso di errore
            }
        }
        return null; // Restituisce null se non è stato possibile leggere i dati
    }

    /**
     * Gestisce il pacchetto di richiesta ricevuto dal client, passando il pacchetto al gestore dei messaggi
     * per la gestione della logica del pacchetto. Se viene prodotto un pacchetto di risposta, viene aggiunto
     * alla coda delle risposte.
     *
     * @param packet Il pacchetto di richiesta da gestire.
     */
    public void handlePacket(Request_ResponseMessage packet) {
        Request_ResponseMessage responsePacket = hotelierMessageManager.handlePacket(packet); // Gestisce il pacchetto
        if (responsePacket != null) {
            synchronized (responseQueue) {
                responseQueue.add(responsePacket); // Aggiungi la risposta alla coda
            }
        }
    }

    /**
     * Gestisce la scrittura dei pacchetti di risposta verso il client.
     * Se la coda delle risposte contiene dei pacchetti, li serializza e li scrive sul canale.
     * Dopo aver scritto un pacchetto, rimuove il pacchetto dalla coda se è stato completamente inviato.
     */
    public void handleWrite() {
        if (isConnected) {
            try {
                // Se non è stato ancora inizializzato, prepara il buffer di risposta
                if (responseBuffer == null) {
                    synchronized (responseQueue) {
                        Request_ResponseMessage responsePacket = responseQueue.peek(); // Prendi il primo pacchetto nella coda
                        if (responsePacket != null) {
                            responseBuffer = serializeResponse(responsePacket); // Serializza il pacchetto
                        }
                    }
                }

                // Scrive il pacchetto di risposta sul canale
                if (responseBuffer != null) {
                    client.write(responseBuffer);
                    // Se il pacchetto è stato scritto completamente, rimuovilo dalla coda
                    if (!responseBuffer.hasRemaining()) {
                        synchronized (responseQueue) {
                            responseQueue.poll(); // Rimuovi il pacchetto dalla coda
                        }
                        responseBuffer = null; // Resetta il buffer di risposta
                    }
                }

            } catch (IOException exception) {
                isConnected = false; // Imposta la connessione come chiusa in caso di errore
            }
        }
    }

    /**
     * Serializza il pacchetto di risposta in un buffer di byte che può essere inviato al client.
     *
     * @param packet Il pacchetto di risposta da serializzare.
     * @return Il buffer di byte serializzato che rappresenta il pacchetto di risposta.
     */
    private ByteBuffer serializeResponse(Request_ResponseMessage packet) {
        try {
            int packetID = messageSerialization.getIDFromPacket(packet); // Ottieni l'ID del pacchetto
            if (packetID != -1) {
                byte[] serializedPacket = objectMapper.writeValueAsBytes(packet); // Serializza il pacchetto in byte[]
                int payloadSize = serializedPacket.length;
                ByteBuffer serializedResponse = ByteBuffer.allocate(4 + 4 + payloadSize).order(ByteOrder.BIG_ENDIAN);
                serializedResponse.putInt(payloadSize); // Aggiungi la dimensione del payload
                serializedResponse.putInt(packetID); // Aggiungi l'ID del pacchetto
                serializedResponse.put(serializedPacket); // Aggiungi i dati serializzati
                serializedResponse.flip(); // Imposta la posizione del buffer per la lettura
                return serializedResponse;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Restituisce null se c'è stato un errore nella serializzazione
    }

    /**
     * Deserializza il pacchetto di richiesta a partire dai dati binari del payload.
     *
     * @param packetID L'ID del pacchetto per determinare il tipo di pacchetto.
     * @param requestPayload Il buffer del payload da deserializzare.
     * @return Il pacchetto deserializzato.
     */
    private Request_ResponseMessage deserializeRequest(int packetID, ByteBuffer requestPayload) {
        byte[] payloadBytes = new byte[requestPayload.remaining()]; // Converte il payload in un array di byte
        requestPayload.get(payloadBytes);
        try {
            // Deserializza il pacchetto dal byte[] usando ObjectMapper
            Request_ResponseMessage packet = objectMapper.readValue(payloadBytes, Request_ResponseMessage.class);
            return packet; // Restituisce il pacchetto deserializzato
        } catch (IOException e) {
            System.err.println("Error deserializing request: " + e.getMessage());
            return null; // Restituisce null se c'è stato un errore nella deserializzazione
        }
    }

    /**
     * Verifica se il client è ancora connesso.
     *
     * @return true se il client è connesso, false altrimenti.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Chiude la connessione con il client e gestisce la disconnessione.
     *
     * @throws IOException Se si verifica un errore durante la chiusura della connessione.
     */
    public void close() throws IOException {
        hotelierMessageManager.handleClientDisconnect(); // Gestisce la disconnessione del client
        client.close(); // Chiude il canale di comunicazione con il client
    }
}
