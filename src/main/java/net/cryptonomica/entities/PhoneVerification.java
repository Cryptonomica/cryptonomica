package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

@Entity // -> net.cryptonomica.service.OfyService
// @Cache // --- ? can be turned off to show changes faster
public class PhoneVerification {
    /* ---- Logger */
    // private static final Logger LOG = Logger.getLogger(PhoneVerification.class.getName());
    /*------------ */
    @Id
    // @Id fields cannot be filtered on !!!!
    private String fingerprint; //.............................1
    @Index
    // --- for filters in Objectify:
    // .filter("fingerprintStr", fingerprint.toUpperCase())
    // like: 05600EB8208485E6942666E06A7B21E2844C7980
    private String fingerprintStr; //..........................2
    @Index
    private String phoneNumber; // +972523333333 ..............3
    @Index
    // RandomStringUtils.randomNumeric(7)
    private String smsMessage; //..............................5
    @Index
    private Email userEmail; //................................6
    @Index
    private Date smsMessageSend; //............................7
    @Index
    private Boolean verified; //...............................8
    @Index
    private Date entityCreated; //.............................9
    @Index
    private Integer failedVerificationAttemps; //.............10

    /* --- Constructors: */
    public PhoneVerification() {
        this.verified = false;
        this.entityCreated = new Date();
        this.failedVerificationAttemps = 0;
    } // end: empty constructor

    public PhoneVerification(String fingerprint) {
        this.verified = false;
        this.entityCreated = new Date();
        this.fingerprint = fingerprint;
        this.fingerprintStr = fingerprint;
    }

    /* ------- Methods - */

    /* ----- Getters and Setters:  */

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getFingerprintStr() {
        return fingerprintStr;
    }

    public void setFingerprintStr(String fingerprintStr) {
        this.fingerprintStr = fingerprintStr;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSmsMessage() {
        return smsMessage;
    }

    public void setSmsMessage(String smsMessage) {
        this.smsMessage = smsMessage;
    }

    public Email getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(Email userEmail) {
        this.userEmail = userEmail;
    }

    public Date getSmsMessageSend() {
        return smsMessageSend;
    }

    public void setSmsMessageSend(Date smsMessageSend) {
        this.smsMessageSend = smsMessageSend;
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
}
