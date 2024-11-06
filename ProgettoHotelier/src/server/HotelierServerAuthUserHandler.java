package server;

import model.Utente;
import org.apache.commons.lang3.StringUtils;
import utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static server.ServerJsonSettings.USERS_PATH_JSON;

public class HotelierServerAuthUserHandler {
    public static HotelierServerAuthUserHandler instance = null;

    public static HotelierServerAuthUserHandler getInstance(){
        if(instance == null){
            instance = new HotelierServerAuthUserHandler();
        }
        return instance;
    }

    //lista di tutti gli utenti
    private List<Utente> users;

    private HotelierServerAuthUserHandler(){
        users = new ArrayList<>();
    }

    public Utente Authentication (String username, String password){
        synchronized (users){
            for(Utente user: users){
                if(StringUtils.equalsIgnoreCase(username,user.getUsername()) && StringUtils.equalsIgnoreCase(password,user.getPassword())){
                    return user;
                }
            }
        }
        return null;
    }

    public String register(String username, String password){
        if(username.isEmpty() || password.isEmpty()){
            return "Errore: Username e Password vuoti";
        }
        if(StringUtils.containsWhitespace(username) || password.isEmpty()){
            return "Errore Caratteri: Spazi vuoti non permessi in Username e Password";
        }

        synchronized (users){
            for(Utente user : users){
                if (StringUtils.equalsIgnoreCase(username,user.getUsername())){
                    return "Errore Registrazione: Utente già presente";
                }
            }

            Utente user = new Utente(username,password);
            users.add(user);
        }
        serialize();

        return  "Nuovo Utente : " + username + " è stato registrato con successo";
    }

    public Utente getUserByName(String username){
        synchronized (users){
            for(Utente user : users){
                if(StringUtils.equalsIgnoreCase(username,user.getUsername())){
                    return user;
                }
            }
        }
        return null;
    }

    public void serialize(){
        try{
            synchronized (users){
                String jsonUser = JsonUtils.serialize(users);
                JsonUtils.writeFile(jsonUser, new File(USERS_PATH_JSON));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void deserialize(){
        try{
            synchronized (users){
             var userFile = new File(USERS_PATH_JSON);
             var userJson = JsonUtils.readFile(userFile);
             var deserializedUsers = Arrays.asList(JsonUtils.deserialize(userJson,Utente[].class));
             users.addAll(deserializedUsers);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
