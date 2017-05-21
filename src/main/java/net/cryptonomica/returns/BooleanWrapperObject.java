package net.cryptonomica.returns;

import java.io.Serializable;

/**
 * We can not return Boolean from API directly, thus we need a wrapper
 */
public class BooleanWrapperObject implements Serializable {

    private Boolean result;
    private String message;
    private String errorMessage;

    /* --- Constructors:  */

    public BooleanWrapperObject() {
    }

    public BooleanWrapperObject(Boolean result) {
        this.result = result;
    }

    public BooleanWrapperObject(Boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    /* --- Getters and Setters: */

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
