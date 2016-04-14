package net.cryptonomica.returns;

import java.io.Serializable;

/**
 * Simple String Wrapper Object
 */
public class StringWrapperObject implements Serializable{
    private String message;
    private String error;

    public StringWrapperObject() {
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
