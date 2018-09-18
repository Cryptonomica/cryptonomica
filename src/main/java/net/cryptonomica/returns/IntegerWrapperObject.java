package net.cryptonomica.returns;

import java.io.Serializable;

/**
 * simple Integer wrapper object;
 */
public class IntegerWrapperObject implements Serializable {

    private Integer number;
    private String message;
    private String error;

    /* ---- Constructors: */

    public IntegerWrapperObject() {
    }

    public IntegerWrapperObject(Integer number) {
        this.number = number;
    }

    /* ---- Getters and Setters: */

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
