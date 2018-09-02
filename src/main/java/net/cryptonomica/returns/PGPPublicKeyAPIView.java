package net.cryptonomica.returns;

import net.cryptonomica.entities.OnlineVerification;
import net.cryptonomica.entities.PGPPublicKeyData;

import java.io.Serializable;
import java.util.Date;

/**
 * Key representation that can be sent when requested using API for partner services
 */
public class PGPPublicKeyAPIView implements Serializable {

    /* ----- Logger: */
    // private static final Logger LOG = Logger.getLogger(PGPPublicKeyAPIView.class.getName());
    /* ----- */

    private String fingerprint; //.........................1
    private String cryptonomicaUserId; //..................2
    private String keyID; //...............................3
    private String userID; //..............................4
    private String firstName; //...........................5
    private String lastName; //............................6
    private String userEmail; //...........................7
    private String userPhoneNumber; //.....................8
    private Date created; //...............................9
    private Date exp; //..................................10
    private Integer bitStrength; //.......................11
    private String asciiArmored; //.......................12
    private String nationality; //........................13
    private Date birthdate; // ...........................14
    private Boolean verifiedOffline; //...................15
    private Boolean verifiedOnline; //....................16
    private Boolean revoked; //...........................17
    private Date revokedOn; //............................18
    private String revokedBy; //..........................19
    private String revocationNotes; //....................20

    // PGPPublicKeyData has also: @Parent private Key<CryptonomicaUser> cryptonomicaUserKey;
    // we have cryptonomicaUserId + webSafeString and not need it

    /* ----- Constructors: */
    public PGPPublicKeyAPIView() {
    }

    public PGPPublicKeyAPIView(PGPPublicKeyData pgpPublicKeyData, OnlineVerification onlineVerification) {
        // 1
        this.fingerprint = pgpPublicKeyData.getFingerprint();
        // 2
        this.cryptonomicaUserId = pgpPublicKeyData.getCryptonomicaUserId();
        // 3
        this.keyID = pgpPublicKeyData.getKeyID();
        // 4
        this.userID = pgpPublicKeyData.getUserID();
        // 5
        this.firstName = pgpPublicKeyData.getFirstName();
        // 6
        this.lastName = pgpPublicKeyData.getLastName();
        // 7
        if (pgpPublicKeyData.getUserEmail() != null) {
            this.userEmail = pgpPublicKeyData.getUserEmail().getEmail();
        }
        // 8
        if (onlineVerification == null) {
            this.userPhoneNumber = null;
        } else {
            this.userPhoneNumber = onlineVerification.getPhoneNumber();
        }
        // 9
        this.created = pgpPublicKeyData.getCreated();
        // 10
        this.exp = pgpPublicKeyData.getExp();
        // 11
        this.bitStrength = pgpPublicKeyData.getBitStrength();
        // 12
        if (pgpPublicKeyData.getAsciiArmored() != null) {
            this.asciiArmored = pgpPublicKeyData.getAsciiArmored().getValue();
        }
        // 13
        this.nationality = pgpPublicKeyData.getNationality();
        // 14
        this.birthdate = pgpPublicKeyData.getUserBirthday();
        // 15
        this.verifiedOffline = pgpPublicKeyData.getVerifiedOffline();
        // 16
        this.verifiedOnline = pgpPublicKeyData.getVerifiedOnline();
        // 17
        this.revoked = pgpPublicKeyData.getRevoked();
        // 18
        this.revokedOn = pgpPublicKeyData.getRevokedOn();
        // 19
        this.revokedBy = pgpPublicKeyData.getRevokedBy();

        // LOG
        // LOG.warning(new Gson().toJson(this));

    } // end: constructor

    public PGPPublicKeyAPIView(PGPPublicKeyData pgpPublicKeyData, String userPhoneNumber) {
        // 1
        this.fingerprint = pgpPublicKeyData.getFingerprint();
        // 2
        this.cryptonomicaUserId = pgpPublicKeyData.getCryptonomicaUserId();
        // 3
        this.keyID = pgpPublicKeyData.getKeyID();
        // 4
        this.userID = pgpPublicKeyData.getUserID();
        // 5
        this.firstName = pgpPublicKeyData.getFirstName();
        // 6
        this.lastName = pgpPublicKeyData.getLastName();
        // 7
        if (pgpPublicKeyData.getUserEmail() != null) {
            this.userEmail = pgpPublicKeyData.getUserEmail().getEmail();
        }
        // 8
        this.userPhoneNumber = userPhoneNumber;
        // 9
        this.created = pgpPublicKeyData.getCreated();
        // 10
        this.exp = pgpPublicKeyData.getExp();
        // 11
        this.bitStrength = pgpPublicKeyData.getBitStrength();
        // 12
        if (pgpPublicKeyData.getAsciiArmored() != null) {
            this.asciiArmored = pgpPublicKeyData.getAsciiArmored().getValue();
        }
        // 13
        this.nationality = pgpPublicKeyData.getNationality();
        // 14
        this.birthdate = pgpPublicKeyData.getUserBirthday();
        // 15
        this.verifiedOffline = pgpPublicKeyData.getVerifiedOffline();
        // 16
        this.verifiedOnline = pgpPublicKeyData.getVerifiedOnline();
        // 17
        this.revoked = pgpPublicKeyData.getRevoked();
        // 18
        this.revokedOn = pgpPublicKeyData.getRevokedOn();
        // 19
        this.revokedBy = pgpPublicKeyData.getRevokedBy();

        // LOG
        // LOG.warning(new Gson().toJson(this));

    } // end: constructor

    public PGPPublicKeyAPIView(PGPPublicKeyData pgpPublicKeyData) {
        // 1
        this.fingerprint = pgpPublicKeyData.getFingerprint();
        // 2
        this.cryptonomicaUserId = pgpPublicKeyData.getCryptonomicaUserId();
        // 3
        this.keyID = pgpPublicKeyData.getKeyID();
        // 4
        this.userID = pgpPublicKeyData.getUserID();
        // 5
        this.firstName = pgpPublicKeyData.getFirstName();
        // 6
        this.lastName = pgpPublicKeyData.getLastName();
        // 7
        if (pgpPublicKeyData.getUserEmail() != null) {
            this.userEmail = pgpPublicKeyData.getUserEmail().getEmail();
        }
        // 8
        this.userPhoneNumber = null; // < ! , to be assigned with setter method
        // 9
        this.created = pgpPublicKeyData.getCreated();
        // 10
        this.exp = pgpPublicKeyData.getExp();
        // 11
        this.bitStrength = pgpPublicKeyData.getBitStrength();
        // 12
        if (pgpPublicKeyData.getAsciiArmored() != null) {
            this.asciiArmored = pgpPublicKeyData.getAsciiArmored().getValue();
        }
        // 13
        this.nationality = pgpPublicKeyData.getNationality();
        // 14
        this.birthdate = pgpPublicKeyData.getUserBirthday();
        // 15
        this.verifiedOffline = pgpPublicKeyData.getVerifiedOffline();
        // 16
        this.verifiedOnline = pgpPublicKeyData.getVerifiedOnline();
        // 17
        this.revoked = pgpPublicKeyData.getRevoked();
        // 18
        this.revokedOn = pgpPublicKeyData.getRevokedOn();
        // 19
        this.revokedBy = pgpPublicKeyData.getRevokedBy();

        // LOG
        // LOG.warning(new Gson().toJson(this));

    } // end: constructor


    /* ----- Getters and Setters: */

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getCryptonomicaUserId() {
        return cryptonomicaUserId;
    }

    public void setCryptonomicaUserId(String cryptonomicaUserId) {
        this.cryptonomicaUserId = cryptonomicaUserId;
    }

    public String getKeyID() {
        return keyID;
    }

    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getExp() {
        return exp;
    }

    public void setExp(Date exp) {
        this.exp = exp;
    }

    public Integer getBitStrength() {
        return bitStrength;
    }

    public void setBitStrength(Integer bitStrength) {
        this.bitStrength = bitStrength;
    }

    public String getAsciiArmored() {
        return asciiArmored;
    }

    public void setAsciiArmored(String asciiArmored) {
        this.asciiArmored = asciiArmored;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public Boolean getVerifiedOffline() {
        return verifiedOffline;
    }

    public void setVerifiedOffline(Boolean verifiedOffline) {
        this.verifiedOffline = verifiedOffline;
    }

    public Boolean getVerifiedOnline() {
        return verifiedOnline;
    }

    public void setVerifiedOnline(Boolean verifiedOnline) {
        this.verifiedOnline = verifiedOnline;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public Date getRevokedOn() {
        return revokedOn;
    }

    public void setRevokedOn(Date revokedOn) {
        this.revokedOn = revokedOn;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }

    public String getRevocationNotes() {
        return revocationNotes;
    }

    public void setRevocationNotes(String revocationNotes) {
        this.revocationNotes = revocationNotes;
    }

}
