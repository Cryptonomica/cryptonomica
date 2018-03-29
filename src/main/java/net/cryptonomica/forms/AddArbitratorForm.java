package net.cryptonomica.forms;

import com.google.appengine.api.datastore.Text;

import java.io.Serializable;

/**
 * form to add an arbitrator
 */
public class AddArbitratorForm implements Serializable {

    // the same as cryptonomica user id and google user id;
    private String id; // ............................. ......1
    private String arbitratorInfo; // ............. ..........2
    private String linkedInProfileLink; // ...................3
    private String webSiteLink; //............................4

    /* --- Constructors: */

    public AddArbitratorForm() {
    }

    /* --- Getters and Setters: */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArbitratorInfo() {
        return arbitratorInfo;
    }

    public void setArbitratorInfo(String arbitratorInfo) {
        this.arbitratorInfo = arbitratorInfo;
    }

    public String getLinkedInProfileLink() {
        return linkedInProfileLink;
    }

    public void setLinkedInProfileLink(String linkedInProfileLink) {
        this.linkedInProfileLink = linkedInProfileLink;
    }

    public String getWebSiteLink() {
        return webSiteLink;
    }

    public void setWebSiteLink(String webSiteLink) {
        this.webSiteLink = webSiteLink;
    }
}
