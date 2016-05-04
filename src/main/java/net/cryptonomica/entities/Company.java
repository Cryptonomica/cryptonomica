package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

import java.util.ArrayList;

/**
 * Company
 */
@Entity
@Cache
public class Company {
    @Id
    Long id;
    @Index
    String name;
    @Index
    ArrayList<String> previousNames;
    @Index
    String state; // state of registration
    @Index
    String registrationNumber; //
    @Index
    Key<CryptonomicaUser> createdBy;
    Key<Verification> companyVerification;
    Text companyInfo;
    @Load
    ArrayList<Ref<AuthorityToRepresent>> representedBy;
    @Index
    Boolean active; // will be shown in search results (// paid for verification)

    public Company() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getPreviousNames() {
        return previousNames;
    }

    public void setPreviousNames(ArrayList<String> previousNames) {
        this.previousNames = previousNames;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Key<CryptonomicaUser> getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Key<CryptonomicaUser> createdBy) {
        this.createdBy = createdBy;
    }

    public Key<Verification> getCompanyVerification() {
        return companyVerification;
    }

    public void setCompanyVerification(Key<Verification> companyVerification) {
        this.companyVerification = companyVerification;
    }

    public Text getCompanyInfo() {
        return companyInfo;
    }

    public void setCompanyInfo(Text companyInfo) {
        this.companyInfo = companyInfo;
    }

    public ArrayList<Ref<AuthorityToRepresent>> getRepresentedBy() {
        return representedBy;
    }

    public void setRepresentedBy(ArrayList<Ref<AuthorityToRepresent>> representedBy) {
        this.representedBy = representedBy;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
