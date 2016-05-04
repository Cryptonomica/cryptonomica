package net.cryptonomica.forms;

import java.io.Serializable;

/**
 * from
 * String keyOwnerCryptonomicaUserId and String fingerprint;
 * we can than create Key<PGPPublicKeyData>
 */
public class VerifyPGPPublicKeyForm implements Serializable {
    //
    private String webSafeString; // ..............1
    private String verificationInfo; // ...........2
    private String basedOnDocument; // ............3

    /* ----- Constructors:  */

    public VerifyPGPPublicKeyForm() {
    } // end: empty args constructor

    /* ----- Getters and Setters: */

    public String getWebSafeString() {
        return webSafeString;
    }

    public void setWebSafeString(String webSafeString) {
        this.webSafeString = webSafeString;
    }

    public String getVerificationInfo() {
        return verificationInfo;
    }

    public void setVerificationInfo(String verificationInfo) {
        this.verificationInfo = verificationInfo;
    }

    public String getBasedOnDocument() {
        return basedOnDocument;
    }

    public void setBasedOnDocument(String basedOnDocument) {
        this.basedOnDocument = basedOnDocument;
    }
}
