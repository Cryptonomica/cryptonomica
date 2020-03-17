package net.cryptonomica.forms;

import java.io.Serializable;

/**
 * form for payments (except for verification)
 */
public class StripePaymentFormGeneral implements Serializable {

    // see required data on:
    // https://github.com/stripe/stripe-java#usage

    private String cardNumber; // ...................................................1
    private Integer cardExpMonth; // number from 1 to 12) ...........................2
    private Integer cardExpYear; // number from 2020 to 2030 ........................3
    private Integer cardCVC; // .....................................................4
    private String nameOnCard; // ...................................................5
    private String cardHolderEmail; //  .............................................6
    private String paymentFor; // ...................................................7
    private double priceInEuro; // ..................................................8
    private int priceInCents; // ....................................................9
    private String paymentTypeCode; // .............................................10

    /* --- Constructors: */

    public StripePaymentFormGeneral() {
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

    public String getNameOnCard() {
        return nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    public String getCardHolderEmail() {
        return cardHolderEmail;
    }

    public void setCardHolderEmail(String cardHolderEmail) {
        this.cardHolderEmail = cardHolderEmail;
    }

    public String getPaymentFor() {
        return paymentFor;
    }

    public void setPaymentFor(String paymentFor) {
        this.paymentFor = paymentFor;
    }

    public double getPriceInEuro() {
        return priceInEuro;
    }

    public void setPriceInEuro(double priceInEuro) {
        this.priceInEuro = priceInEuro;
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
