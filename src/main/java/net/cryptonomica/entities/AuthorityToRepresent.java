package net.cryptonomica.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

import java.util.Date;

/**
 *
 */
@Entity
@Cache
public class AuthorityToRepresent {
    @Parent
    Key<Company> companyKey;
    @Id
    Long id;
    Key<CryptonomicaUser> representedBy;
    String typeOfPower;
    Date validFrom;
    Date validUntil;
    Key<Verification> verificationKey;
    @Index
    Boolean active; // will be shown in search results (// paid for verification)

    public AuthorityToRepresent() {
    }

    public Key<Company> getCompanyKey() {
        return companyKey;
    }

    public void setCompanyKey(Key<Company> companyKey) {
        this.companyKey = companyKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Key<CryptonomicaUser> getRepresentedBy() {
        return representedBy;
    }

    public void setRepresentedBy(Key<CryptonomicaUser> representedBy) {
        this.representedBy = representedBy;
    }

    public String getTypeOfPower() {
        return typeOfPower;
    }

    public void setTypeOfPower(String typeOfPower) {
        this.typeOfPower = typeOfPower;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Key<Verification> getVerificationKey() {
        return verificationKey;
    }

    public void setVerificationKey(Key<Verification> verificationKey) {
        this.verificationKey = verificationKey;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
