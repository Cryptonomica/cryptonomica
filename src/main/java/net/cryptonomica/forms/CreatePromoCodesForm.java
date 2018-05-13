package net.cryptonomica.forms;

import com.google.appengine.api.datastore.Email;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class CreatePromoCodesForm implements Serializable {

    private Integer discountInPercent; // price = price - ((price/100)*discountInPercent)
    private Date validUntil;
    private List<Email> validOnlyForUsers;
    private List<String> validOnlyForCountries;
    private Integer numberOfPromoCodesToCreate;

    /* --- Constructor */
    public CreatePromoCodesForm() {
    }

    /* --- Getters and Setters */

    public Integer getDiscountInPercent() {
        return discountInPercent;
    }

    public void setDiscountInPercent(Integer discountInPercent) {
        this.discountInPercent = discountInPercent;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
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

    public Integer getNumberOfPromoCodesToCreate() {
        return numberOfPromoCodesToCreate;
    }

    public void setNumberOfPromoCodesToCreate(Integer numberOfPromoCodesToCreate) {
        this.numberOfPromoCodesToCreate = numberOfPromoCodesToCreate;
    }

}
