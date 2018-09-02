package net.cryptonomica.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.io.Serializable;
import java.util.HashMap;

@Entity // -> net.cryptonomica.service.OfyService
public class MyBookmarks implements Serializable {

    @Id
    private String userId; // the same as 'Cryptonomica User ID' and 'Google User ID'
    private HashMap<String, String> users;
    private HashMap<String, String> savedKeys;

    /* --- Constructors */

    public MyBookmarks() {
        this.users = new HashMap<>();
        this.savedKeys = new HashMap<>();
    }

    public MyBookmarks(String userId) {
        this.userId = userId;
        this.users = new HashMap<>();
        this.savedKeys = new HashMap<>();
    }

    /* --- Methods */

    public void addUser(String userId, String name) {
        this.users.put(userId, name);
    }

    public void removeUser(String userId) {
        this.users.remove(userId);
    }

    public void addKey(String fingerprint, String keyID) {
        this.savedKeys.put(fingerprint, keyID);
    }

    public void removeKey(String fingerprint) {
        this.savedKeys.remove(fingerprint);
    }

    /* --- Getters and Setters */

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public HashMap<String, String> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, String> users) {
        this.users = users;
    }

    public HashMap<String, String> getSavedKeys() {
        return savedKeys;
    }

    public void setSavedKeys(HashMap<String, String> savedKeys) {
        this.savedKeys = savedKeys;
    }
}
