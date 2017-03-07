package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import net.cryptonomica.forms.AddArbitratorForm;

import java.util.ArrayList;

/**
 *
 */
@Entity // -> net.cryptonomica.service.OfyService
//@Cache
public class Arbitrator {
    @Id
    String id; // the same as cryptonomica user id and google user id;
    ArrayList<Key<ArbitratorLicence>> licencies;
    Text arbitratorInfo; // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Text
    @Index
    Boolean active; // will be shown in search results (// verified and active - can be done manually)
    Link linkedInProfileLink;
    Link webSiteLink;


    /* --- Constructors: */
    public Arbitrator() {
    }

    public Arbitrator(AddArbitratorForm addArbitratorForm) {
        // required:
        this.id = addArbitratorForm.getId();
        // optional:
        if (addArbitratorForm.getArbitratorInfo() != null) {
            this.arbitratorInfo = new Text(addArbitratorForm.getArbitratorInfo());
        }
        if (addArbitratorForm.getLinkedInProfileLink() != null) {
            this.linkedInProfileLink = new Link(addArbitratorForm.getLinkedInProfileLink());
        }
        if (addArbitratorForm.getWebSiteLink() != null) {
            this.webSiteLink = new Link(addArbitratorForm.getWebSiteLink());
        }
    }

    /* --- Getters and Setters: */
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

    public Text getArbitratorInfo() {
        return arbitratorInfo;
    }

    public void setArbitratorInfo(Text arbitratorInfo) {
        this.arbitratorInfo = arbitratorInfo;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Link getLinkedInProfileLink() {
        return linkedInProfileLink;
    }

    public void setLinkedInProfileLink(Link linkedInProfileLink) {
        this.linkedInProfileLink = linkedInProfileLink;
    }

    public Link getWebSiteLink() {
        return webSiteLink;
    }

    public void setWebSiteLink(Link webSiteLink) {
        this.webSiteLink = webSiteLink;
    }
}
