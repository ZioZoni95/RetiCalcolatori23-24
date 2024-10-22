package server.networkPackages;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RegisterResponsePacket {
    private boolean success;
    private String message;

    // Costruttore vuoto richiesto da Jackson per la deserializzazione
    public RegisterResponsePacket() {}

    // Costruttore con parametri
    public RegisterResponsePacket(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getter e Setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Metodo per serializzare l'oggetto in una stringa JSON
    public String toJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    // Metodo per deserializzare una stringa JSON in un oggetto RegisterResponsePacket
    public static RegisterResponsePacket fromJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, RegisterResponsePacket.class);
    }

    // Metodo di rappresentazione dell'oggetto sotto forma di stringa
    @Override
    public String toString() {
        return "RegisterResponsePacket{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
