package net.cryptonomica.forms;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
public class NewUserRegistrationForm implements Serializable {
    String armoredPublicPGPkeyBlock;
    Date birthday;
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

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
}
