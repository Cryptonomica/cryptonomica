package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

import java.util.Date;

/**
 * for securiry reasons we can track and show to user his latest logins
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
    Date loginDate;
    @Index
    Email loginEmail; //
    String IP;
    String userBrowser;
    String userOS;
    String hostname;
    String city;
    String region;
    String country;
    String provider; // "org" (provider) (f.e.: "AS1680 013 NetVision Ltd")

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
}
