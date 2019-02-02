package net.cryptonomica.forms;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
public class NewUserRegistrationForm implements Serializable {

    String armoredPublicPGPkeyBlock;
    Date birthday; // legacy
    Integer birthdayYear;
    Integer birthdayMonth;
    Integer birthdayDay;
    String userInfo;

    public NewUserRegistrationForm() {
    }

    // ---- Getters and Setters:

    public String getArmoredPublicPGPkeyBlock() {
        return armoredPublicPGPkeyBlock;
    }

    public void setArmoredPublicPGPkeyBlock(String armoredPublicPGPkeyBlock) {
        this.armoredPublicPGPkeyBlock = armoredPublicPGPkeyBlock;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

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

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

}
