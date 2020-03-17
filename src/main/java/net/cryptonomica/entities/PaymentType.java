package net.cryptonomica.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import net.cryptonomica.forms.CreatePaymentTypeForm;

import java.io.Serializable;
import java.util.Date;

@Entity // -> net.cryptonomica.service.OfyService
public class PaymentType implements Serializable {
    @Id
    private String paymentTypeCode; // ..............................................1
    private String paymentFor; // ...................................................2
    private Integer priceInCents; // ................................................3
    private Date entityCreatedOn; // ................................................4
    private String entityCreatedBy; // ..............................................5

    /* Constructor */

    public PaymentType() {
    }

    public PaymentType(CreatePaymentTypeForm createPaymentTypeForm) {
        this.paymentTypeCode = createPaymentTypeForm.getPaymentTypeCode();
        this.paymentFor = createPaymentTypeForm.getPaymentFor();
        this.priceInCents = createPaymentTypeForm.getPriceInCents();
    }

    /* Getters and Setters */

    public String getPaymentTypeCode() {
        return paymentTypeCode;
    }

    public void setPaymentTypeCode(String paymentTypeCode) {
        this.paymentTypeCode = paymentTypeCode;
    }

    public String getPaymentFor() {
        return paymentFor;
    }

    public void setPaymentFor(String paymentFor) {
        this.paymentFor = paymentFor;
    }

    public Integer getPriceInCents() {
        return priceInCents;
    }

    public void setPriceInCents(Integer priceInCents) {
        this.priceInCents = priceInCents;
    }

    public Date getEntityCreatedOn() {
        return entityCreatedOn;
    }

    public void setEntityCreatedOn(Date entityCreatedOn) {
        this.entityCreatedOn = entityCreatedOn;
    }

    public String getEntityCreatedBy() {
        return entityCreatedBy;
    }

    public void setEntityCreatedBy(String entityCreatedBy) {
        this.entityCreatedBy = entityCreatedBy;
    }

}
