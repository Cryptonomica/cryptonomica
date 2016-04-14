package net.cryptonomica.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Data about payment
 */
@Entity
@Cache
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
