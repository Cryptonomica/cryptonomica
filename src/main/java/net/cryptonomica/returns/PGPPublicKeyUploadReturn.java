package net.cryptonomica.returns;

import net.cryptonomica.entities.PGPPublicKeyData;

import java.io.Serializable;

/**
 *
 */
public class PGPPublicKeyUploadReturn implements Serializable {
    String messageToUser;
    PGPPublicKeyGeneralView pgpPublicKeyGeneralView;

    public PGPPublicKeyUploadReturn() {
    }

    public PGPPublicKeyUploadReturn(String messageToUser, PGPPublicKeyData pgpPublicKeyData) {
        this.messageToUser = messageToUser;
        this.pgpPublicKeyGeneralView = new PGPPublicKeyGeneralView(pgpPublicKeyData);
    } // end of constructor

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