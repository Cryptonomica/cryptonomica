package net.cryptonomica.forms;

import java.io.Serializable;

/*
 * For for creation payment type data
 * (for admins only) *
 * */

public class CreatePaymentTypeForm implements Serializable {

    private String paymentFor; // ...................................................1
    private Integer priceInCents; // ................................................2
    private String paymentTypeCode; // ..............................................3

    /* Constructor */

    public CreatePaymentTypeForm() {
    }

    /* Getters and Setters */

    public String getPaymentFor() {
        return paymentFor;
    }

    public void setPaymentFor(String paymentFor) {
        this.paymentFor = paymentFor;
    }

    public int getPriceInCents() {
        return priceInCents;
    }

    public void setPriceInCents(int priceInCents) {
        this.priceInCents = priceInCents;
    }

    public String getPaymentTypeCode() {
        return paymentTypeCode;
    }

    public void setPaymentTypeCode(String paymentTypeCode) {
        this.paymentTypeCode = paymentTypeCode;
    }

}
