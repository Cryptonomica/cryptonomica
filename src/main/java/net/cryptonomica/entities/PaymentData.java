package net.cryptonomica.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

import java.util.Date;

/**
 * Data about payment
 * --- not used yet
 */
@Entity // -> net.cryptonomica.service.OfyService
// @Cache
public class PaymentData {

    @Id
    Long Id;
    @Parent
    Key<Verification> paidFor;
    @Index
    Date paidOn;
    @Index
    String paymentMethod;

    public PaymentData() {
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Key<Verification> getPaidFor() {
        return paidFor;
    }

    public void setPaidFor(Key<Verification> paidFor) {
        this.paidFor = paidFor;
    }

    public Date getPaidOn() {
        return paidOn;
    }

    public void setPaidOn(Date paidOn) {
        this.paidOn = paidOn;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
