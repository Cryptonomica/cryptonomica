package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * for recording important actions and changes
 */
@Entity // -> net.cryptonomica.service.OfyService
@Cache
public class CryptonomicaLog implements Serializable {

    /* ---- Properties */
    @Id
    private Long id;

    @Index
    private Date date;

    @Index
    private String userWhoseDataIsChanged; // cryptonomica user ID

    @Index
    private String userWhoseDataIsChangedEmail; // lowercase

    @Index
    private String changedBy; // Cryptonomica user ID (who did the action)

    @Index
    private String changedByEmail; // email of a user, who did the action/change

    // description of the action (what was done)
    @Index
    private String action;

    // @Index
    private List<Key> changedEntities;
    //    private List<Object> changedEntitiesObjects;
    private List<String> changedEntitiesStringRepresentation;

    /* ---- Constructor */

    public CryptonomicaLog() {
        this.date = new Date();
        this.changedEntities = new ArrayList<>();
//        this.changedEntitiesObjects = new ArrayList<>();
        this.changedEntitiesStringRepresentation = new ArrayList<>();
    }

    public CryptonomicaLog(String action, String userWhoseDataIsChanged, String userWhoseDataIsChangedEmail, String changedBy, String changedByEmail) {
        this.date = new Date();
        this.changedEntities = new ArrayList<>();
//        this.changedEntitiesObjects = new ArrayList<>();
        this.changedEntitiesStringRepresentation = new ArrayList<>();
        this.action = action.toLowerCase();
        this.userWhoseDataIsChanged = userWhoseDataIsChanged;
        this.userWhoseDataIsChangedEmail = userWhoseDataIsChangedEmail.toLowerCase();
        this.changedBy = changedBy;
        this.changedByEmail = changedByEmail.toLowerCase();
    }

    /* ---- Methods */

    public Boolean addChangedEntityKey(Key key) {
        return this.changedEntities.add(key);
    }

//    public Boolean addChangedEntityObject(Object entityObject) {
//        return this.changedEntitiesObjects.add(entityObject);
//    }

    public Boolean addChangedEntityStringRepresentation(String jsonSting) {
        return this.changedEntitiesStringRepresentation.add(jsonSting);
    }


    /* ---- [customized] Getters and Setters */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserWhoseDataIsChanged() {
        return userWhoseDataIsChanged;
    }

    public void setUserWhoseDataIsChanged(String userWhoseDataIsChanged) {
        this.userWhoseDataIsChanged = userWhoseDataIsChanged;
    }

    public String getUserWhoseDataIsChangedEmail() {
        return userWhoseDataIsChangedEmail.toLowerCase();
    }

    public void setUserWhoseDataIsChangedEmail(String userWhoseDataIsChangedEmail) {
        this.userWhoseDataIsChangedEmail = userWhoseDataIsChangedEmail.toLowerCase();
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangedByEmail() {
        return changedByEmail.toLowerCase();
    }

    public void setChangedByEmail(String changedByEmail) {
        this.changedByEmail = changedByEmail.toLowerCase();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action.toLowerCase();
    }

    public List<Key> getChangedEntities() {
        return changedEntities;
    }

    public void setChangedEntities(List<Key> changedEntities) {
        this.changedEntities = changedEntities;
    }

//    public List<Object> getChangedEntitiesObjects() {
//        return changedEntitiesObjects;
//    }
//
//    public void setChangedEntitiesObjects(List<Object> changedEntitiesObjects) {
//        this.changedEntitiesObjects = changedEntitiesObjects;
//    }


    public List<String> getChangedEntitiesStringRepresentation() {
        return changedEntitiesStringRepresentation;
    }

    public void setChangedEntitiesStringRepresentation(List<String> changedEntitiesStringRepresentation) {
        this.changedEntitiesStringRepresentation = changedEntitiesStringRepresentation;
    }

}
