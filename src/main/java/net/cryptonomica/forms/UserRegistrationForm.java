package net.cryptonomica.forms;

import com.google.appengine.api.datastore.Text;

import java.io.Serializable;
import java.util.Date;

/**
 * JavaBean
 * represents form for user registration
 */
public class UserRegistrationForm implements Serializable {
    String firstName;   // ? if user changes firstname?
    String lastName;    // ? from invitation // ? if user changes lastname?
    Date birthday;      //
    String userInfo;      // user provided info

    public UserRegistrationForm() {
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

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
}
