package net.cryptonomica.forms;

import java.io.Serializable;

/**
 * form to add an arbitrator
 */
public class StripePaymentForm implements Serializable {

    // see required data on:
    // https://github.com/stripe/stripe-java#usage

    private String cardNumber; // ...................................................1
    private Integer cardExpMonth; // number from 1 to 12) ...........................2
    private Integer cardExpYear; // number from 2017 to 2030 ........................3
    private Integer cardCVC; // .....................................................4
    private String fingerprint; // ..................................................5
    private String cardHolderFirstName; // as in OpenPGP key certificate ............6
    private String cardHolderLastName; // as in OpenPGP key certificate .............7
    private String promoCode; // ....................................................8

    /* --- Constructors: */

    public StripePaymentForm() {
        //
    }

    /* --- Getters and Setters: */

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Integer getCardExpMonth() {
        return cardExpMonth;
    }

    public void setCardExpMonth(Integer cardExpMonth) {
        this.cardExpMonth = cardExpMonth;
    }

    public Integer getCardExpYear() {
        return cardExpYear;
    }

    public void setCardExpYear(Integer cardExpYear) {
        this.cardExpYear = cardExpYear;
    }

    public Integer getCardCVC() {
        return cardCVC;
    }

    public void setCardCVC(Integer cardCVC) {
        this.cardCVC = cardCVC;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getCardHolderFirstName() {
        return cardHolderFirstName;
    }

    public void setCardHolderFirstName(String cardHolderFirstName) {
        this.cardHolderFirstName = cardHolderFirstName;
    }

    public String getCardHolderLastName() {
        return cardHolderLastName;
    }

    public void setCardHolderLastName(String cardHolderLastName) {
        this.cardHolderLastName = cardHolderLastName;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }
}
