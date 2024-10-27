package client;

public class HotelierClientMain {
    final static int DEFAULT_PORT = 9999;

    public static void main(String[] args){
        int myPort = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                myPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Fornire il numero di porta come intero");
                System.exit(-1);
            }
        }
        // crea e avvia il client
        HotelierNIOClient client = new HotelierNIOClient(myPort);
        client.start();
    }
}
