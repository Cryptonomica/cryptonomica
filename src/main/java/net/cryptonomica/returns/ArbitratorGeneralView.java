package net.cryptonomica.returns;

import net.cryptonomica.entities.Arbitrator;
import net.cryptonomica.entities.CryptonomicaUser;

import java.io.Serializable;

/**
 *
 */
public class ArbitratorGeneralView implements Serializable {

    // from Arbitrator Entity:
    String id; // the same as cryptonomica user id and google user id;
    String arbitratorInfo; // from: Text
    Boolean active; // will be shown in search results (// verified and active - can be done manually)
    String linkedInProfileLink; // from Link
    String webSiteLink; // from Link

    // from Cryptonomica User Entity:
    String firstName;
    String lastName;

    /* --- Constructors: */

    public ArbitratorGeneralView() {
    }

    public ArbitratorGeneralView(CryptonomicaUser arbitratorCryptonomicaUser, Arbitrator arbitrator) {
        this.id = arbitrator.getId();
        if (arbitrator.getArbitratorInfo() != null) {
            this.arbitratorInfo = arbitrator.getArbitratorInfo().getValue();
        }
        this.active = arbitrator.getActive();
        if (arbitrator.getLinkedInProfileLink() != null) {
            this.linkedInProfileLink = arbitrator.getLinkedInProfileLink().getValue();
        }
        if (arbitrator.getWebSiteLink() != null) {
            this.webSiteLink = arbitrator.getWebSiteLink().getValue();
        }
        if (arbitratorCryptonomicaUser.getFirstName() != null) {
            this.firstName = arbitratorCryptonomicaUser.getFirstName();
        }
        if (arbitratorCryptonomicaUser.getLastName() != null) {
            this.lastName = arbitratorCryptonomicaUser.getLastName();
        }
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
