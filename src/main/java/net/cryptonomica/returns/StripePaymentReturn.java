package net.cryptonomica.returns;

import java.io.Serializable;

/**
 *
 */
public class StripePaymentReturn implements Serializable {

    private Boolean result; // if payment successful 'true'................1
    private String messageToUser; // custom message to user ...............2

    /* --- Constructors: */

    public StripePaymentReturn() {
    }

    /* --- Getters and Setters: */

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getMessageToUser() {
        return messageToUser;
    }

    public void setMessageToUser(String messageToUser) {
        this.messageToUser = messageToUser;
    }
}
