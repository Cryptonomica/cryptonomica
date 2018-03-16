package net.cryptonomica.returns;

import java.io.Serializable;

/**
 *
 */
public class NewUserRegistrationReturn implements Serializable {
    String messageToUser;
    PGPPublicKeyGeneralView pgpPublicKeyGeneralView;
    UserProfileGeneralView userProfileGeneralView;
    LoginView loginView;

    public NewUserRegistrationReturn() {
    }

    public NewUserRegistrationReturn(
            String messageToUser,
            PGPPublicKeyGeneralView pgpPublicKeyGeneralView,
            UserProfileGeneralView userProfileGeneralView,
            LoginView loginView) {
        this.messageToUser = messageToUser;
        this.pgpPublicKeyGeneralView = pgpPublicKeyGeneralView;
        this.userProfileGeneralView = userProfileGeneralView;
        this.loginView = loginView;
    } // end of constructor

    // ------ Getters and Setters:

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

    public UserProfileGeneralView getUserProfileGeneralView() {
        return userProfileGeneralView;
    }

    public void setUserProfileGeneralView(UserProfileGeneralView userProfileGeneralView) {
        this.userProfileGeneralView = userProfileGeneralView;
    }

    public LoginView getLoginView() {
        return loginView;
    }

    public void setLoginView(LoginView loginView) {
        this.loginView = loginView;
    }
}
