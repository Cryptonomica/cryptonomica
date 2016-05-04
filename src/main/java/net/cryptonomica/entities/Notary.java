package net.cryptonomica.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import net.cryptonomica.forms.AddNotaryForm;

import java.util.ArrayList;

/**
 *
 */
@Entity
@Cache
public class Notary {
    @Id String id; // the same id as in cryptonomica user or google user
    // what happends if notary with this Id alreade exists? -
    ArrayList<Key<NotaryLicence>> licences;
    String info;
    Boolean active; // will be shown in search results (// paid for verification)


    public Notary() {
        this.licences = new ArrayList<Key<NotaryLicence>>();
    }

    public Notary(AddNotaryForm addNotaryForm, Key<NotaryLicence> notaryLicenceKey) {
        this.id = addNotaryForm.getNotaryCryptonomicaUserID();
        this.info = addNotaryForm.getNotaryInfo();
        this.licences = new ArrayList<Key<NotaryLicence>>();
        this.licences.add(notaryLicenceKey);
    } // constructor for Notary from AddNotaryForm

    // --- Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Key<NotaryLicence>> getLicences() {
        return licences;
    }

    public void setLicences(ArrayList<Key<NotaryLicence>> licences) {
        this.licences = licences;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
