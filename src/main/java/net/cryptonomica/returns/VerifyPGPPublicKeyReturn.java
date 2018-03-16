package net.cryptonomica.returns;

import java.io.Serializable;

/**
 *
 */
public class VerifyPGPPublicKeyReturn implements Serializable {
    String MessageToUser;
    VerificationGeneralView verificationGeneralView;
    PGPPublicKeyGeneralView pgpPublicKeyGeneralView;
    UserProfileGeneralView userProfileGeneralView;

    public VerifyPGPPublicKeyReturn() {
    }

    public VerifyPGPPublicKeyReturn(
            String messageToUser,
            VerificationGeneralView verificationGeneralView,
            PGPPublicKeyGeneralView pgpPublicKeyGeneralView,
            UserProfileGeneralView userProfileGeneralView) {
        MessageToUser = messageToUser;
        this.verificationGeneralView = verificationGeneralView;
        this.pgpPublicKeyGeneralView = pgpPublicKeyGeneralView;
        this.userProfileGeneralView = userProfileGeneralView;
    }  // end of constructor

    // ----- Getters and Setters

    public String getMessageToUser() {
        return MessageToUser;
    }

    public void setMessageToUser(String messageToUser) {
        MessageToUser = messageToUser;
    }

    public VerificationGeneralView getVerificationGeneralView() {
        return verificationGeneralView;
    }

    public void setVerificationGeneralView(VerificationGeneralView verificationGeneralView) {
        this.verificationGeneralView = verificationGeneralView;
    }

    public PGPPublicKeyGeneralView getPgpPublicKeyGeneralView() {
        return pgpPublicKeyGeneralView;
    }

    public void setPgpPublicKeyGeneralView(PGPPublicKeyGeneralView pgpPublicKeyGeneralView) {
        this.pgpPublicKeyGeneralView = pgpPublicKeyGeneralView;
    }

    public UserProfileGeneralView getUserProfileGeneralView() {
        return userProfileGeneralView;
    }

    public void setUserProfileGeneralView(UserProfileGeneralView userProfileGeneralView) {
        this.userProfileGeneralView = userProfileGeneralView;
    }
}
