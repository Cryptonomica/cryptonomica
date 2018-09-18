package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

import java.util.Date;

/**
 * for security reasons we can track and show to user his latest logins
 * -- need to define when to save new logins (may be if some info changes)
 */
@Entity // -> net.cryptonomica.service.OfyService
@Cache
public class Login {
    @Parent
    Key<CryptonomicaUser> cryptonomicaUserKey; //
    @Id
    Long id;
    @Index
    private Date loginDate;
    @Index
    private String userAction;
    @Index
    private Email loginEmail; //
    @Index
    private String IP;
    @Index
    private String userBrowser;
    @Index
    private String userOS;
    @Index
    private String hostname;
    @Index
    private String city;
    @Index
    private String region;
    @Index
    private String country;
    @Index
    private String provider; // "org" (provider) (f.e.: "AS1680 013 NetVision Ltd")
    // no index
    private String userAgentString;

    public Login() {
    }

    // ------ Getters and Setters:


    public Key<CryptonomicaUser> getCryptonomicaUserKey() {
        return cryptonomicaUserKey;
    }

    public void setCryptonomicaUserKey(Key<CryptonomicaUser> cryptonomicaUserKey) {
        this.cryptonomicaUserKey = cryptonomicaUserKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public String getUserAction() {
        return userAction;
    }

    public void setUserAction(String userAction) {
        this.userAction = userAction;
    }

    public Email getLoginEmail() {
        return loginEmail;
    }

    public void setLoginEmail(Email loginEmail) {
        this.loginEmail = loginEmail;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getUserBrowser() {
        return userBrowser;
    }

    public void setUserBrowser(String userBrowser) {
        this.userBrowser = userBrowser;
    }

    public String getUserOS() {
        return userOS;
    }

    public void setUserOS(String userOS) {
        this.userOS = userOS;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUserAgentString() {
        return userAgentString;
    }

    public void setUserAgentString(String userAgentString) {
        this.userAgentString = userAgentString;
    }

}
