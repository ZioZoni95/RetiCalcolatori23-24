package server.config;

import utils.JsonUtils;

import java.io.File;
import java.io.IOException;

import static server.config.ServerJsonSettings.SERVER_CONFIG_JSON;

public class ConfigManager {
    private static HotelierServerConfig serverConfig;

    public ConfigManager(){}

    public static void createDefeaultSesttings(){
        try{
            serverConfig = new HotelierServerConfig(9999, 1099, 49152, 10, "localhost", "Hotelier-Service-Program", "230.0.0.0");
            var jsonConfig = JsonUtils.serialize(serverConfig);
            JsonUtils.writeFile(jsonConfig, new File(SERVER_CONFIG_JSON));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void loadServerConfig() {
        try {
            var configJSON = JsonUtils.readFile(new File(SERVER_CONFIG_JSON));
            serverConfig = JsonUtils.deserialize(configJSON, HotelierServerConfig.class);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // restituisce server config
    public static HotelierServerConfig getServerConfig() {
        return serverConfig;
    }

}
