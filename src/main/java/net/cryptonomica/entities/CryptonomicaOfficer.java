package net.cryptonomica.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import com.google.appengine.api.datastore.Text;

import java.util.ArrayList;

/**
 *
 */
@Entity // -> net.cryptonomica.service.OfyService
@Cache
public class CryptonomicaOfficer {
    @Id
    String id; // the same as cryptonomica user id and google user id;
    ArrayList<Key<CryptonomicaOfficerLicence>> licencies;
    Text info;
    @Index
    Boolean active; // will be shown in search results (// paid for verification)

    public CryptonomicaOfficer() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Key<CryptonomicaOfficerLicence>> getLicencies() {
        return licencies;
    }

    public void setLicencies(ArrayList<Key<CryptonomicaOfficerLicence>> licencies) {
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
