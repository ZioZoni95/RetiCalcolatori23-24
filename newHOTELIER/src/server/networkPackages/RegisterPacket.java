package server.networkPackages;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RegisterPacket {
    private String username;
    private String password;

    // Costruttore vuoto richiesto da Jackson per la deserializzazione
    public RegisterPacket() {}

    // Costruttore con parametri
    public RegisterPacket(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter e Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Metodo per serializzare l'oggetto in una stringa JSON
    public String toJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    // Metodo per deserializzare una stringa JSON in un oggetto RegisterPacket
    public static RegisterPacket fromJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, RegisterPacket.class);
    }

    // Metodo di rappresentazione dell'oggetto sotto forma di stringa
    @Override
    public String toString() {
        return "RegisterPacket{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
