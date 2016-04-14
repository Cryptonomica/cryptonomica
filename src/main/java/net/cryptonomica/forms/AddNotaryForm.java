package net.cryptonomica.forms;

import java.io.Serializable;
import java.util.Date;

/**
 * form to add notary and his/her first licence
 */
public class AddNotaryForm implements Serializable {
    // id of a notary (licenceOwner in licence)
    private String notaryCryptonomicaUserID; //.1
    // info about a notary
    private String notaryInfo; //...............2
    private String licenceIssuedBy; //..........3
    private String licenceCountry; // ..........4
    private Date licenceIssuedOn; //............5
    private Date licenceValidUntil; //..........6
    private String licenceInfo; //..............7
    private String verificationInfo; //.........8

    /* ------- Constructors:  */
    public AddNotaryForm() {
    } // end: empty args constructor

    /* ----- Getters ans Setters: */

    public String getNotaryCryptonomicaUserID() {
        return notaryCryptonomicaUserID;
    }

    public void setNotaryCryptonomicaUserID(String notaryCryptonomicaUserID) {
        this.notaryCryptonomicaUserID = notaryCryptonomicaUserID;
    }

    public String getNotaryInfo() {
        return notaryInfo;
    }

    public void setNotaryInfo(String notaryInfo) {
        this.notaryInfo = notaryInfo;
    }

    public String getLicenceIssuedBy() {
        return licenceIssuedBy;
    }

    public void setLicenceIssuedBy(String licenceIssuedBy) {
        this.licenceIssuedBy = licenceIssuedBy;
    }

    public String getLicenceCountry() {
        return licenceCountry;
    }

    public void setLicenceCountry(String licenceCountry) {
        this.licenceCountry = licenceCountry;
    }

    public Date getLicenceIssuedOn() {
        return licenceIssuedOn;
    }

    public void setLicenceIssuedOn(Date licenceIssuedOn) {
        this.licenceIssuedOn = licenceIssuedOn;
    }

    public Date getLicenceValidUntil() {
        return licenceValidUntil;
    }

    public void setLicenceValidUntil(Date licenceValidUntil) {
        this.licenceValidUntil = licenceValidUntil;
    }

    public String getLicenceInfo() {
        return licenceInfo;
    }

    public void setLicenceInfo(String licenceInfo) {
        this.licenceInfo = licenceInfo;
    }

    public String getVerificationInfo() {
        return verificationInfo;
    }

    public void setVerificationInfo(String verificationInfo) {
        this.verificationInfo = verificationInfo;
    }
}
