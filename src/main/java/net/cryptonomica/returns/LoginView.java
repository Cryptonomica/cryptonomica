package net.cryptonomica.returns;

import net.cryptonomica.entities.Login;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by viktor on 23/02/16.
 */
public class LoginView implements Serializable {
    Long loginId;
    Date loginDate;
    String loginEmail; // < from com.google.appengine.api.datastore.Email
    String IP;
    String userBrowser;
    String userOS;
    String hostname;
    String city;
    String region;
    String country;
    String provider;

    public LoginView() {
    }

    public LoginView(Login login) {
        this.loginId = login.getId();
        this.loginDate = login.getLoginDate();
        if (login.getLoginEmail() != null) {
            this.loginEmail = login.getLoginEmail().getEmail();
        }
        this.IP = login.getIP();
        this.userBrowser = login.getUserBrowser();
        this.userOS = login.getUserOS();
        this.hostname = login.getHostname();
        this.city = login.getCity();
        this.region = login.getRegion();
        this.country = login.getCountry();
        this.provider = login.getProvider();
    } // end of constructor

    // ----- Getters and Setters:

    public Long getLoginId() {
        return loginId;
    }

    public void setLoginId(Long loginId) {
        this.loginId = loginId;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public String getLoginEmail() {
        return loginEmail;
    }

    public void setLoginEmail(String loginEmail) {
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
