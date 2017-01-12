package net.cryptonomica.returns;

import net.cryptonomica.entities.PGPPublicKeyData;

import java.io.Serializable;

/**
 *
 */
public class PGPPublicKeyUploadReturn implements Serializable {

    private String messageToUser;
    private PGPPublicKeyGeneralView pgpPublicKeyGeneralView;

    /* --- Constructors: */

    public PGPPublicKeyUploadReturn() {
    }

    public PGPPublicKeyUploadReturn(String messageToUser, PGPPublicKeyGeneralView pgpPublicKeyGeneralView) {
        this.messageToUser = messageToUser;
        this.pgpPublicKeyGeneralView = pgpPublicKeyGeneralView;
    }

    public PGPPublicKeyUploadReturn(String messageToUser, PGPPublicKeyData pgpPublicKeyData) {
        this.messageToUser = messageToUser;
        this.pgpPublicKeyGeneralView = new PGPPublicKeyGeneralView(pgpPublicKeyData);
    }

    // ------- Getters and Setters:

    public String getMessageToUser() {
        return messageToUser;
    }

    public void setMessageToUser(String messageToUser) {
        this.messageToUser = messageToUser;
    }

    public PGPPublicKeyGeneralView getPgpPublicKeyGeneralView() {
        return pgpPublicKeyGeneralView;
    }

    public void setPgpPublicKeyGeneralView(PGPPublicKeyGeneralView pgpPublicKeyGeneralView) {
        this.pgpPublicKeyGeneralView = pgpPublicKeyGeneralView;
    }
}
