package net.cryptonomica.returns;

import com.googlecode.objectify.annotation.Index;
import net.cryptonomica.entities.CryptonomicaUser;

import java.io.Serializable;
import java.util.Date;

/**
 * The object server returns in response to user registration form
 */
public class UserRegistrationReturn implements Serializable {

    private String userId;
    private String webSafeStringKey; // see: savedCryptonomicaUserKey.toWebSafeString();
    private String firstName;
    private String lastName;

    // private Date birthday;
    private Integer birthdayYear;
    private Integer birthdayMonth;
    private Integer birthdayDay;

    private String email;
    private String userInfo;
    private String messageToUser;

    public UserRegistrationReturn() {
    }

    public UserRegistrationReturn(CryptonomicaUser cryptonomicaUser, String messageToUser) {
        this.userId = cryptonomicaUser.getUserId();
        this.firstName = cryptonomicaUser.getFirstName();
        this.lastName = cryptonomicaUser.getLastName();

        // this.birthday = cryptonomicaUser.getBirthday();
        this.birthdayYear = cryptonomicaUser.getBirthdayYear();
        this.birthdayMonth = cryptonomicaUser.getBirthdayMonth();
        this.birthdayDay = cryptonomicaUser.getBirthdayDay();

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

//    public Date getBirthday() {
//        return birthday;
//    }
//
//    public void setBirthday(Date birthday) {
//        this.birthday = birthday;
//    }


    public Integer getBirthdayYear() {
        return birthdayYear;
    }

    public void setBirthdayYear(Integer birthdayYear) {
        this.birthdayYear = birthdayYear;
    }

    public Integer getBirthdayMonth() {
        return birthdayMonth;
    }

    public void setBirthdayMonth(Integer birthdayMonth) {
        this.birthdayMonth = birthdayMonth;
    }

    public Integer getBirthdayDay() {
        return birthdayDay;
    }

    public void setBirthdayDay(Integer birthdayDay) {
        this.birthdayDay = birthdayDay;
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
