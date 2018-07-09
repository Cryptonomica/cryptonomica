package net.cryptonomica.returns;

import net.cryptonomica.entities.PGPPublicKeyData;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Key representation that can be sent to frontend
 */
public class PGPPublicKeyGeneralView implements Serializable {
    /* ----- Logger: */
    // private static final Logger LOG = Logger.getLogger(PGPPublicKeyGeneralView.class.getName());
    /* ----- */
    // @Id
    private String fingerprint; //.........................1
    //    private String webSafeString; //.......................2
    private String cryptonomicaUserId; //..................3
    private String keyID; //...............................4
    private String userID; //..............................5
    private String firstName; //...........................6
    private String lastName; //............................7
    private String userEmail; //...........................8
    private Date created; //...............................9
    private Date exp; //..................................10
    private Integer bitStrength; //.......................11
    private String asciiArmored; //.......................12
    private Boolean verified; //off-line verification.....13 // legacy
    private Boolean active; //............................14
    private List<String> verificationsWebSafeStrings; //..15
    private List<Long> verificationIDs; // ...............16
    // OnlineVerification entity ID -> fingerprint
    private Boolean onlineVerificationFinished; //........17
    private String nationality; //........................18
    private Date birthdate; // ...........................19
    // new (2018-05-23):
    private Boolean verifiedOffline; //...................20 // <TODO: new
    private Boolean verifiedOnline; //....................21 // <TODO: new
    private Boolean revoked; //...........................22
    private Date revokedOn; //............................23
    private String revokedBy; //..........................24

    // PGPPublicKeyData has also: @Parent private Key<CryptonomicaUser> cryptonomicaUserKey;
    // we have cryptonomicaUserId + webSafeString and not need it

    /* ----- Constructors: */
    public PGPPublicKeyGeneralView() {
    }

    public PGPPublicKeyGeneralView(PGPPublicKeyData pgpPublicKeyData) {

        this.fingerprint = pgpPublicKeyData.getFingerprint();

//        if (pgpPublicKeyData.getWebSafeString() == null) {
//            this.webSafeString =
//                    Key.create(
//                            Key.create(CryptonomicaUser.class, pgpPublicKeyData.getCryptonomicaUserId()),
//                            PGPPublicKeyData.class,
//                            pgpPublicKeyData.getFingerprint()
//                    ).toWebSafeString();
//        } else {
//            this.webSafeString = pgpPublicKeyData.getWebSafeString();
//        }

        this.cryptonomicaUserId = pgpPublicKeyData.getCryptonomicaUserId();
        this.keyID = pgpPublicKeyData.getKeyID();
        this.userID = pgpPublicKeyData.getUserID();
        this.firstName = pgpPublicKeyData.getFirstName();
        this.lastName = pgpPublicKeyData.getLastName();

        if (pgpPublicKeyData.getUserEmail() != null) {
            this.userEmail = pgpPublicKeyData.getUserEmail().getEmail();
        }

        this.created = pgpPublicKeyData.getCreated();
        this.exp = pgpPublicKeyData.getExp();
        this.bitStrength = pgpPublicKeyData.getBitStrength();

        if (pgpPublicKeyData.getAsciiArmored() != null) {
            this.asciiArmored = pgpPublicKeyData.getAsciiArmored().getValue();
        }

        if (pgpPublicKeyData.getVerified() == null) {
            this.verified = Boolean.FALSE;
        } else {
            this.verified = pgpPublicKeyData.getVerified();
        }

        this.active = pgpPublicKeyData.getActive();

        this.verificationsWebSafeStrings = pgpPublicKeyData.getVerificationsWebSafeStrings();

        this.onlineVerificationFinished = pgpPublicKeyData.getOnlineVerificationFinished();

        this.nationality = pgpPublicKeyData.getNationality();

        this.birthdate = pgpPublicKeyData.getUserBirthday();
        // 20
        this.verifiedOffline = pgpPublicKeyData.getVerifiedOffline();
        // 21
        this.verifiedOnline = pgpPublicKeyData.getVerifiedOnline();
        // 22
        this.revoked = pgpPublicKeyData.getRevoked();
        // 23
        this.revokedOn = pgpPublicKeyData.getRevokedOn();
        // 24
        this.revokedBy = pgpPublicKeyData.getRevokedBy();

        // LOG
        // LOG.warning(new Gson().toJson(this));

    } // end: constructor from PGPPublicKeyData obj

    /* ----- Getters and Setters: */

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

//    public String getWebSafeString() {
//        return webSafeString;
//    }
//
//    public void setWebSafeString(String webSafeString) {
//        this.webSafeString = webSafeString;
//    }

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

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<String> getVerificationsWebSafeStrings() {
        return verificationsWebSafeStrings;
    }

    public void setVerificationsWebSafeStrings(List<String> verificationsWebSafeStrings) {
        this.verificationsWebSafeStrings = verificationsWebSafeStrings;
    }

    public List<Long> getVerificationIDs() {
        return verificationIDs;
    }

    public void setVerificationIDs(List<Long> verificationIDs) {
        this.verificationIDs = verificationIDs;
    }

    public Boolean getOnlineVerificationFinished() {
        return onlineVerificationFinished;
    }

    public void setOnlineVerificationFinished(Boolean onlineVerificationFinished) {
        this.onlineVerificationFinished = onlineVerificationFinished;
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

}
