package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;

/**
 *
 */
@Entity
@Cache
public class Lawyer {
    @Id
    String id; // same as cryptonomica user and google user id
    ArrayList<Key<LawyerLicence>> lawyerLicencies;
    Text info;
    Boolean active; // will be shown in search results (// paid for verification)

    public Lawyer() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Key<LawyerLicence>> getLawyerLicencies() {
        return lawyerLicencies;
    }

    public void setLawyerLicencies(ArrayList<Key<LawyerLicence>> lawyerLicencies) {
        this.lawyerLicencies = lawyerLicencies;
    }

    public Text getInfo() {
        return info;
    }

    public void setInfo(Text info) {
        this.info = info;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
