package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

/**
 * Data about payment via Stripe for online key verification
 */
@Entity // -> net.cryptonomica.service.OfyService
// @Cache
public class StripePaymentForKeyVerification {

    @Id
    private Long id; //........................................1
    @Index
    private Boolean verified; //...............................2
    @Index
    private Date entityCreated; //.............................3
    @Index
    private Integer failedVerificationAttemps; //..............4
    @Index
    private String paymentVerificationCode; // ................5
    @Index
    private String fingerprint; // key fingerprint ............6
    @Index
    private Email userEmail; // ...............................7
    @Index
    // the same as Google user ID ( googleUser.getUserId() )
    // = com.google.appengine.api.users.User
    // (java.lang.String userId)
    private String cryptonomicaUserId; //......................8
    // @Index
    private String chargeStr; //...............................9
    // @Index
    private String chargeJsonStr; //..........................10
    @Index
    // f.e.: "id": "ch_1AEomQDjN2J4PGOBQqToH0Kz"
    private String chargeId; // ..............................11
    private Key<Login> getPriceForKeyVerificationLogin;//.....12
    private Key<Login> chargeLogin; //........................13
    private Key<Login> checkPaymentVerificationCodeLogin; //..14

    /* ---- Constructors */

    public StripePaymentForKeyVerification() {
        this.entityCreated = new Date();
        this.failedVerificationAttemps = 0;
    }

    /* ---- Getters and Setters */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Date getEntityCreated() {
        return entityCreated;
    }

    public void setEntityCreated(Date entityCreated) {
        this.entityCreated = entityCreated;
    }

    public Integer getFailedVerificationAttemps() {
        return failedVerificationAttemps;
    }

    public void setFailedVerificationAttemps(Integer failedVerificationAttemps) {
        this.failedVerificationAttemps = failedVerificationAttemps;
    }

    public String getPaymentVerificationCode() {
        return paymentVerificationCode;
    }

    public void setPaymentVerificationCode(String paymentVerificationCode) {
        this.paymentVerificationCode = paymentVerificationCode;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public Email getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(Email userEmail) {
        this.userEmail = userEmail;
    }

    public String getCryptonomicaUserId() {
        return cryptonomicaUserId;
    }

    public void setCryptonomicaUserId(String cryptonomicaUserId) {
        this.cryptonomicaUserId = cryptonomicaUserId;
    }

    public String getChargeStr() {
        return chargeStr;
    }

    public void setChargeStr(String chargeStr) {
        this.chargeStr = chargeStr;
    }

    public String getChargeJsonStr() {
        return chargeJsonStr;
    }

    public void setChargeJsonStr(String chargeJsonStr) {
        this.chargeJsonStr = chargeJsonStr;
    }

    public String getChargeId() {
        return chargeId;
    }

    public void setChargeId(String chargeId) {
        this.chargeId = chargeId;
    }

    public Key<Login> getChargeLogin() {
        return chargeLogin;
    }

    public void setChargeLogin(Key<Login> chargeLogin) {
        this.chargeLogin = chargeLogin;
    }

    public Key<Login> getCheckPaymentVerificationCodeLogin() {
        return checkPaymentVerificationCodeLogin;
    }

    public void setCheckPaymentVerificationCodeLogin(Key<Login> checkPaymentVerificationCodeLogin) {
        this.checkPaymentVerificationCodeLogin = checkPaymentVerificationCodeLogin;
    }

    public Key<Login> getGetPriceForKeyVerificationLogin() {
        return getPriceForKeyVerificationLogin;
    }

    public void setGetPriceForKeyVerificationLogin(Key<Login> getPriceForKeyVerificationLogin) {
        this.getPriceForKeyVerificationLogin = getPriceForKeyVerificationLogin;
    }
}

