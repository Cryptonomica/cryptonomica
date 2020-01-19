package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Entity // -> net.cryptonomica.service.OfyService
public class PromoCode
        implements Serializable { // returned in net.cryptonomica.api.StripePaymentsAPI.createPromoCode

    @Id
    private String promoCode;
    @Index
    private Integer discountInPercent; // price = price - ((price/100)*discountInPercent)
    @Index
    private Boolean used;
    @Index
    private Date validUntil;
    @Index
    private Date entityCreated;
    @Index
    private String createdBy;
    @Index
    private Email usedBy;
    @Index
    private String usedForFingerprint;
    @Index
    private Date usedOn;
    //
    private List<Email> validOnlyForUsers;
    private List<String> validOnlyForCountries; // as in OnlineVerification object

    /* --- Constructors */

    public PromoCode() {
    }

//    public PromoCode(Integer discountInPercent) {
//        this.used = false;
//        this.promoCode = RandomStringUtils.randomAlphanumeric(11);
//        // this.entityCreated = new Date();
//        this.validOnlyForUsers = new ArrayList<>();
//        this.validOnlyForCountries = new ArrayList<>();
//        //
//        this.discountInPercent = discountInPercent;
//    }
//
//    public PromoCode(Integer discountInPercent, String createdBy) {
//        this.used = false;
//        this.promoCode = RandomStringUtils.randomAlphanumeric(11);
//        // this.entityCreated = new Date();
//        this.validOnlyForUsers = new ArrayList<>();
//        this.validOnlyForCountries = new ArrayList<>();
//        //
//        this.discountInPercent = discountInPercent;
//        this.createdBy = createdBy;
//    }

//    public PromoCode(Integer discountInPercent, Date validUntil) {
//        this.used = false;
//        this.promoCode = RandomStringUtils.randomAlphanumeric(11);
//        // this.entityCreated = new Date();
//        this.validOnlyForUsers = new ArrayList<>();
//        this.validOnlyForCountries = new ArrayList<>();
//        //
//        this.discountInPercent = discountInPercent;
//        this.validUntil = validUntil;
//    }

    /* --- Methods */

    public Boolean addEmailToValidOnlyForUsers(Email email) {
        if (email == null || email.getEmail() == null || email.getEmail().isEmpty()) {
            throw new IllegalArgumentException("email is null or empty");
        }
        return this.validOnlyForUsers.add(email);
    }

    public Boolean addCountryCodeToValidOnlyForCountries(String countryCode) {
        if (countryCode == null || countryCode.isEmpty() || countryCode.length() != 2) {
            throw new IllegalArgumentException("country code is invalid");
        }
        return this.validOnlyForCountries.add(countryCode);
    }

    /* --- getters and setters */

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public Integer getDiscountInPercent() {
        return discountInPercent;
    }

    public void setDiscountInPercent(Integer discountInPercent) {
        this.discountInPercent = discountInPercent;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Date getEntityCreated() {
        return entityCreated;
    }

    public void setEntityCreated(Date entityCreated) {
        this.entityCreated = entityCreated;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Email getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(Email usedBy) {
        this.usedBy = usedBy;
    }

    public String getUsedForFingerprint() {
        return usedForFingerprint;
    }

    public void setUsedForFingerprint(String usedForFingerprint) {
        this.usedForFingerprint = usedForFingerprint;
    }

    public Date getUsedOn() {
        return usedOn;
    }

    public void setUsedOn(Date usedOn) {
        this.usedOn = usedOn;
    }

    public List<Email> getValidOnlyForUsers() {
        return validOnlyForUsers;
    }

    public void setValidOnlyForUsers(List<Email> validOnlyForUsers) {
        this.validOnlyForUsers = validOnlyForUsers;
    }

    public List<String> getValidOnlyForCountries() {
        return validOnlyForCountries;
    }

    public void setValidOnlyForCountries(List<String> validOnlyForCountries) {
        this.validOnlyForCountries = validOnlyForCountries;
    }
}
