package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import net.cryptonomica.forms.AddNotaryForm;

import java.util.Date;

/**
 *
 */
@Entity
@Cache
public class NotaryLicence {
    @Parent
    Key<CryptonomicaUser> licenceOwner;
    @Id
    Long id;
    @Index
    String issuedBy;
    @Index
    String country;
    @Index
    Date issuedOn;
    @Index
    Date validUntil;

    Key<Verification> verification;

    Text info;

    public NotaryLicence() {
    }

    public NotaryLicence(AddNotaryForm addNotaryForm) {
        this.licenceOwner = Key.create(CryptonomicaUser.class, addNotaryForm.getNotaryCryptonomicaUserID());
        this.issuedBy = addNotaryForm.getLicenceIssuedBy();
        this.country = addNotaryForm.getLicenceCountry();
        this.issuedOn = addNotaryForm.getLicenceIssuedOn();
        this.validUntil = addNotaryForm.getLicenceValidUntil();
        if (addNotaryForm.getLicenceInfo() != null) {
            this.info = new Text(addNotaryForm.getLicenceInfo());
        }
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
