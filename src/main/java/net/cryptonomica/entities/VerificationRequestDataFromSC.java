package net.cryptonomica.entities;


import com.google.gson.Gson;

import java.io.Serializable;

public class VerificationRequestDataFromSC implements Serializable {

    private String unverifiedFingerprint;
    private String signedString;

    /* ----- Constructors */

    public VerificationRequestDataFromSC() {
    }

    public VerificationRequestDataFromSC(String unverifiedFingerprint, String signedString) {
        this.unverifiedFingerprint = unverifiedFingerprint;
        this.signedString = signedString;
    }

    /* ---- to String */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    /* ----- Getters and Setters */

    public String getUnverifiedFingerprint() {
        return unverifiedFingerprint;
    }

    public void setUnverifiedFingerprint(String unverifiedFingerprint) {
        this.unverifiedFingerprint = unverifiedFingerprint;
    }

    public String getSignedString() {
        return signedString;
    }

    public void setSignedString(String signedString) {
        this.signedString = signedString;
    }
}
