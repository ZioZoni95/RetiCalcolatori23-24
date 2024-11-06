package client.config;

import utils.JsonUtils;

import java.io.File;
import java.io.IOException;

import static client.config.ClientConfigSetting.CLIENT_CONFIG_PATH_JSON;

public class ClientConfigManager {
    private static HotelierClientConfig clientConfig;

    public static void createDeafaultSettings(){
        try{

            clientConfig = new HotelierClientConfig(9999, 1099, 49152, "localhost", "Hotelier-Service-Program", "230.0.0.0");
            var jsonConfig = JsonUtils.serialize(clientConfig);
            JsonUtils.writeFile(jsonConfig, new File(CLIENT_CONFIG_PATH_JSON));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void loadConfigCLient(){
        try{
            var jsonConfig = JsonUtils.readFile(new File(CLIENT_CONFIG_PATH_JSON));
            clientConfig = JsonUtils.deserialize(jsonConfig,HotelierClientConfig.class);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static HotelierClientConfig getClientConfig(){
        return clientConfig;
    }
}
