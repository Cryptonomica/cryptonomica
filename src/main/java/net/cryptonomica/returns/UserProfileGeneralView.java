package net.cryptonomica.returns;

import com.google.gson.Gson;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.PGPPublicKeyData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * General view of user profile
 * to be used 1) in ArrayList in search results 2) to show user profie by id
 */
public class UserProfileGeneralView implements Serializable {

    private String userId; //................................................1
    // CryptonomicaUser key as a web safe string
    // what for?
    private String webSafeStringKey; //......................................2
    private String firstName; //.............................................3
    private String lastName; //..............................................4
    private Date birthday; //................................................5
    private String email; //.................................................6
    private String userInfo; //..............................................7
    private String userCurrentImageLink; //..................................8
    private Boolean loggedIn; //.............................................9
    private Boolean registeredCryptonomicaUser; //..........................10
    private Boolean lawyer; //..............................................11
    private Boolean notary; //..............................................12
    private Boolean arbitrator; //..........................................13
    private Boolean cryptonomicaOfficer; //.................................14
    private ArrayList<PGPPublicKeyGeneralView> pgpPublicKeyGeneralViews; //.15

    /* ----- Constructors */
    public UserProfileGeneralView() {
        this.pgpPublicKeyGeneralViews = new ArrayList<>();
    } // end: no args constructor

    public UserProfileGeneralView( // from CryptonomicaUser
                                   CryptonomicaUser cryptonomicaUser
    ) {
        this.userId = cryptonomicaUser.getUserId();
        this.webSafeStringKey = cryptonomicaUser.getWebSafeStringKey();
        this.firstName = cryptonomicaUser.getFirstName();
        this.lastName = cryptonomicaUser.getLastName();
        this.birthday = cryptonomicaUser.getBirthday();
        if (cryptonomicaUser.getEmail() != null) {
            this.email = cryptonomicaUser.getEmail().getEmail();
        }
        if (cryptonomicaUser.getUserInfo() != null) {
            this.userInfo = cryptonomicaUser.getUserInfo().getValue();
        }
        if (cryptonomicaUser.getUserCurrentImageLink() != null) {
            this.userCurrentImageLink =
                    cryptonomicaUser.getUserCurrentImageLink().toString();
        }
        if (cryptonomicaUser.getLawyer() != null) {
            this.lawyer = Boolean.TRUE;
        } else {
            this.lawyer = Boolean.FALSE;
        }
        if (cryptonomicaUser.getNotary() != null) {
            this.notary = Boolean.TRUE;
        } else {
            this.notary = Boolean.FALSE;
        }
        if (cryptonomicaUser.getArbitrator() != null) {
            this.arbitrator = Boolean.TRUE;
        } else {
            this.arbitrator = Boolean.FALSE;
        }
        if (cryptonomicaUser.getCryptonomicaOfficer() != null) {
            this.cryptonomicaOfficer = Boolean.TRUE;
        } else {
            this.cryptonomicaOfficer = Boolean.FALSE;
        }
    } // end: constructor from CryptonomicaUser class

    public UserProfileGeneralView( // from CryptonomicaUser and list of keys
                                   CryptonomicaUser cryptonomicaUser,
                                   List<PGPPublicKeyData> pgpPublicKeyDataList
    ) {
        /*  ----- */
        ArrayList<PGPPublicKeyGeneralView> pgpPublicKeyGeneralViews =
                new ArrayList<>();
        for (PGPPublicKeyData pgpPublicKeyData : pgpPublicKeyDataList) {
            pgpPublicKeyGeneralViews.add(
                    new PGPPublicKeyGeneralView(pgpPublicKeyData)
            );
        }
        /* ----- */
        this.userId = cryptonomicaUser.getUserId();
        this.webSafeStringKey = cryptonomicaUser.getWebSafeStringKey();
        this.firstName = cryptonomicaUser.getFirstName();
        this.lastName = cryptonomicaUser.getLastName();
        this.birthday = cryptonomicaUser.getBirthday();
        if (cryptonomicaUser.getEmail() != null) {
            this.email = cryptonomicaUser.getEmail().getEmail();
        }
        if (cryptonomicaUser.getUserInfo() != null) {
            this.userInfo = cryptonomicaUser.getUserInfo().getValue();
        }
        if (cryptonomicaUser.getUserCurrentImageLink() != null) {
            this.userCurrentImageLink =
                    cryptonomicaUser.getUserCurrentImageLink().toString();
        }
        if (cryptonomicaUser.getLawyer() != null) {
            this.lawyer = Boolean.TRUE;
        } else {
            this.lawyer = Boolean.FALSE;
        }
        if (cryptonomicaUser.getNotary() != null) {
            this.notary = Boolean.TRUE;
        } else {
            this.notary = Boolean.FALSE;
        }
        if (cryptonomicaUser.getArbitrator() != null) {
            this.arbitrator = Boolean.TRUE;
        } else {
            this.arbitrator = Boolean.FALSE;
        }
        if (cryptonomicaUser.getCryptonomicaOfficer() != null) {
            this.cryptonomicaOfficer = Boolean.TRUE;
        } else {
            this.cryptonomicaOfficer = Boolean.FALSE;
        }
        if (pgpPublicKeyGeneralViews != null && pgpPublicKeyGeneralViews.size() > 0) {
            this.pgpPublicKeyGeneralViews = pgpPublicKeyGeneralViews;
        }
    } // end: from CryptonomicaUser and list of keys

    /* ----- Methods */
    public String toJson(){
        return new Gson().toJson(this);
    }

    /* ----- Getters and Settets: */

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

    public String getUserCurrentImageLink() {
        return userCurrentImageLink;
    }

    public void setUserCurrentImageLink(String userCurrentImageLink) {
        this.userCurrentImageLink = userCurrentImageLink;
    }

    public Boolean getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public Boolean getRegisteredCryptonomicaUser() {
        return registeredCryptonomicaUser;
    }

    public void setRegisteredCryptonomicaUser(Boolean registeredCryptonomicaUser) {
        this.registeredCryptonomicaUser = registeredCryptonomicaUser;
    }

    public Boolean getLawyer() {
        return lawyer;
    }

    public void setLawyer(Boolean lawyer) {
        this.lawyer = lawyer;
    }

    public Boolean getNotary() {
        return notary;
    }

    public void setNotary(Boolean notary) {
        this.notary = notary;
    }

    public Boolean getArbitrator() {
        return arbitrator;
    }

    public void setArbitrator(Boolean arbitrator) {
        this.arbitrator = arbitrator;
    }

    public Boolean getCryptonomicaOfficer() {
        return cryptonomicaOfficer;
    }

    public void setCryptonomicaOfficer(Boolean cryptonomicaOfficer) {
        this.cryptonomicaOfficer = cryptonomicaOfficer;
    }

    public ArrayList<PGPPublicKeyGeneralView> getPgpPublicKeyGeneralViews() {
        return pgpPublicKeyGeneralViews;
    }

    public void setPgpPublicKeyGeneralViews(ArrayList<PGPPublicKeyGeneralView> pgpPublicKeyGeneralViews) {
        this.pgpPublicKeyGeneralViews = pgpPublicKeyGeneralViews;
    }

}
