package net.cryptonomica.returns;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Simple String Wrapper Object
 */
public class StringWrapperObject implements Serializable {

    private String message;
    private String error;

    /* ---- to String */

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    /* --- Constructors: */

    public StringWrapperObject() {
    }

    public StringWrapperObject(String message) {
        this.message = message;
    }

    /* --- Getters and Setters: */

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
