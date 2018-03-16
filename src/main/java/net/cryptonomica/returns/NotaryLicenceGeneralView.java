package net.cryptonomica.returns;

import net.cryptonomica.entities.NotaryLicence;
import net.cryptonomica.entities.Verification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class NotaryLicenceGeneralView implements Serializable {
    Long id;
    String issuedBy;
    String country;
    Date issuedOn;
    Date validUntil;
    String info;
    ArrayList<VerificationGeneralView> verificationGeneralViewArrayList;

    public NotaryLicenceGeneralView() {
        this.verificationGeneralViewArrayList = new ArrayList<>();
    }

    public NotaryLicenceGeneralView(NotaryLicence notaryLicence, List<Verification> verificationList) {
        this.id = notaryLicence.getId();
        this.issuedBy = notaryLicence.getIssuedBy();
        this.country = notaryLicence.getCountry();
        this.issuedOn = notaryLicence.getIssuedOn();
        this.validUntil = notaryLicence.getValidUntil();
        if (notaryLicence.getInfo() != null) {
            this.info = notaryLicence.getInfo().getValue();
        }
        this.verificationGeneralViewArrayList = new ArrayList<VerificationGeneralView>();
        for (Verification verificationData : verificationList) {
            this.verificationGeneralViewArrayList.add(new VerificationGeneralView(verificationData));
        }
    } // end of constructor

    // -------- Getters and Setters:

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getIssuedOn() {
        return issuedOn;
    }

    public void setIssuedOn(Date issuedOn) {
        this.issuedOn = issuedOn;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public ArrayList<VerificationGeneralView> getVerificationGeneralViewArrayList() {
        return verificationGeneralViewArrayList;
    }

    public void setVerificationGeneralViewArrayList(ArrayList<VerificationGeneralView> verificationGeneralViewArrayList) {
        this.verificationGeneralViewArrayList = verificationGeneralViewArrayList;
    }
}
