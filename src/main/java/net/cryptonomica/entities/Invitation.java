package net.cryptonomica.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

import java.util.Date;

/**
 *
 */
@Entity // -> net.cryptonomica.service.OfyService
@Cache
public class Invitation {
    @Parent
    Key<CryptonomicaUser> createdBy;
    @Id
    Long Id;
    Date createdOn;
    String firstName;
    String lastName;
    Key<CryptonomicaUser> acceptedBy;
    Date acceptedOn;

    public Invitation() {
    }

    public Key<CryptonomicaUser> getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Key<CryptonomicaUser> createdBy) {
        this.createdBy = createdBy;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
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

    public Key<CryptonomicaUser> getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(Key<CryptonomicaUser> acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public Date getAcceptedOn() {
        return acceptedOn;
    }

    public void setAcceptedOn(Date acceptedOn) {
        this.acceptedOn = acceptedOn;
    }
}

