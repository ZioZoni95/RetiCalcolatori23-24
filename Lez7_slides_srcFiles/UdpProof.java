package Lez7_slides_srcFiles;
import java.net.*;

/**
 * Questa classe è utile per testare e visualizzare le dimensioni dei buffer send/receive di un socket UDP
 * Utilizzo DatagramSocket:
 *      -->L'uso del socket UDP è indicato per applicazioni che non richiedono una connessione affidabile
 */
public class UdpProof {
    public static void main(String[] args) throws Exception{
        DatagramSocket dataS = new DatagramSocket(); //creazione oggetto DatagramSocket,
                                                    // permette la comunicazione tramite UDP.Utilizzato per send/receive
        int r = dataS.getReceiveBufferSize();       //ottengo dimensione buffer di ricezione
        int s = dataS.getSendBufferSize();          //ottengo dimensione buffer di invio
        System.out.println("Receive buffer " + r);
        System.out.println("Send buffer " +s);
    }
}
