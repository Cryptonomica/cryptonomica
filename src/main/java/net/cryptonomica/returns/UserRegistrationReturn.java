package net.cryptonomica.returns;

import net.cryptonomica.entities.CryptonomicaUser;

import java.io.Serializable;
import java.util.Date;

/**
 * The object server returns in response to user registration form
 */
public class UserRegistrationReturn implements Serializable {
    String userId;
    String webSafeStringKey; // see: savedCryptonomicaUserKey.toWebSafeString();
    String firstName;
    String lastName;
    Date birthday;
    String email;
    String userInfo;
    String messageToUser;

    public UserRegistrationReturn() {
    }

    public UserRegistrationReturn(CryptonomicaUser cryptonomicaUser, String messageToUser) {
        this.userId = cryptonomicaUser.getUserId();
        this.firstName = cryptonomicaUser.getFirstName();
        this.lastName = cryptonomicaUser.getLastName();
        this.birthday = cryptonomicaUser.getBirthday();
        if (cryptonomicaUser.getEmail() != null) {
            this.email = cryptonomicaUser.getEmail().getEmail(); // email -> string
        }
        if (cryptonomicaUser.getUserInfo() != null) {
            this.userInfo = cryptonomicaUser.getUserInfo().getValue();
        }
        this.messageToUser = messageToUser;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWebSafeStringKey() {
        return webSafeStringKey;
    }

    public void setWebSafeStringKey(String webSafeStringKey) {
        this.webSafeStringKey = webSafeStringKey;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getMessageToUser() {
        return messageToUser;
    }

    public void setMessageToUser(String messageToUser) {
        this.messageToUser = messageToUser;
    }
}
