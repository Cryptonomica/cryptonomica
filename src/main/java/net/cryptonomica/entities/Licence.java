package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

import java.util.Date;

/**
 * licence for lawyer, notary, arbiter
 */
@Entity // << -- add the to the actual entity
@Cache // << -- add the to the actual entity
public class Licence {
    @Parent
    Key<CryptonomicaUser> licenceOwner;
    @Id
    Long id;
    //the following fields can be not indexed
    String issuedBy;
    String country;
    Date issuedOn;
    Date validUntil;
    Key<Verification> verification;
    Text info;

    public Licence() {
    }

    public Key<CryptonomicaUser> getLicenceOwner() {
        return licenceOwner;
    }

    public void setLicenceOwner(Key<CryptonomicaUser> licenceOwner) {
        this.licenceOwner = licenceOwner;
    }

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

    public Key<Verification> getVerification() {
        return verification;
    }

    public void setVerification(Key<Verification> verification) {
        this.verification = verification;
    }

    public Text getInfo() {
        return info;
    }

    public void setInfo(Text info) {
        this.info = info;
    }
}
