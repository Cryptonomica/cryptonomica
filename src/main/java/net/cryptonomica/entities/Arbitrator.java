package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;

/**
 *
 */
@Entity
@Cache
public class Arbitrator {
    @Id
    String id; // the same as cryptonomica user id and google user id;
    ArrayList<Key<ArbitratorLicence>> licencies;
    Text info; // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Text
    @Index
    Boolean active; // will be shown in search results (// verified and active - can be done manually)

    public Arbitrator() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Key<ArbitratorLicence>> getLicencies() {
        return licencies;
    }

    public void setLicencies(ArrayList<Key<ArbitratorLicence>> licencies) {
        this.licencies = licencies;
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
