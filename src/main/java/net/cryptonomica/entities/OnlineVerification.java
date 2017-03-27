package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Can be verification of: 1) Public PGP key, 2) Licence 3) Company 4) Authority to represent company
 */
@Entity // -> net.cryptonomica.service.OfyService
@Cache
public class OnlineVerification {
    /*---------*/
    private static final Logger LOG = Logger.getLogger(OnlineVerification.class.getName());
    /*-------------*/
    /* Data Fields */
    /* They have to create also a human-readable DB record, thus data can be duplicated to make record self-explaining */
    @Id
    private Long Id; // ..........................................1
    // Objectify Key of PGPPublicKeyData
    // @Index // <-- not neded
    private String PGPPublicKeyDataUrlSafeKey; // ................2
    @Index
    private Email userEmail; //
    @Index
    private String lastName; // ..................................3
    @Index
    private String firstName; // .................................4
    @Index
    // --- for filters in Objectify:
    // .filter("fingerprintStr", fingerprint.toUpperCase())
    // like: 05600EB8208485E6942666E06A7B21E2844C7980
    private String fingerprintStr; //.............................5
    @Index
    // keyID, like [0xE77173E5]
    private String keyID; // .....................................6
    @Index
    private Date entityCreated; //................................7
    // https://github.com/objectify/objectify/wiki/Entities#maps
    Map<String, Date> imageUploadKeysMap; // HashMap<> allows null
    // as a key, returns null if key-value not set
    @Index
    private Date verifiedOn; // ..................................8
    @Index
    private String verifiedByCryptonomicaUserId; // ......../.....9
    private String verificationNotes; // ........................10
    // Place of Birth - as in passport
    String placeOfBirth; //......................................11
    // can be: 'male' or 'female'
    String sex; //...............................................12
    // country names from ngular-country-picker: https://github.com/Puigcerber/angular-country-picker
    String nationalityPrimary; //................................13
    private Integer step; // ....................................14


    /* ----- Constructors: */
    public OnlineVerification() {
        // this.paymentsDataWebSafeStringsList = new ArrayList<>();
        this.entityCreated = new Date();
    } // end empty args constructor

    /* ----- Methods:  */


    /* ----- Getters and Setters:   */

    public String getPGPPublicKeyDataUrlSafeKey() {
        return PGPPublicKeyDataUrlSafeKey;
    }

    public void setPGPPublicKeyDataUrlSafeKey(String PGPPublicKeyDataUrlSafeKey) {
        this.PGPPublicKeyDataUrlSafeKey = PGPPublicKeyDataUrlSafeKey;
    }

    public Email getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(Email userEmail) {
        this.userEmail = userEmail;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFingerprintStr() {
        return fingerprintStr;
    }

    public void setFingerprintStr(String fingerprintStr) {
        this.fingerprintStr = fingerprintStr;
    }

    public String getKeyID() {
        return keyID;
    }

    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    public Date getEntityCreated() {
        return entityCreated;
    }

    public void setEntityCreated(Date entityCreated) {
        this.entityCreated = entityCreated;
    }

    public Map<String, Date> getImageUploadKeysMap() {
        return imageUploadKeysMap;
    }

    public void setImageUploadKeysMap(Map<String, Date> imageUploadKeysMap) {
        this.imageUploadKeysMap = imageUploadKeysMap;
    }

    public Date getVerifiedOn() {
        return verifiedOn;
    }

    public void setVerifiedOn(Date verifiedOn) {
        this.verifiedOn = verifiedOn;
    }

    public String getVerifiedByCryptonomicaUserId() {
        return verifiedByCryptonomicaUserId;
    }

    public void setVerifiedByCryptonomicaUserId(String verifiedByCryptonomicaUserId) {
        this.verifiedByCryptonomicaUserId = verifiedByCryptonomicaUserId;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNationalityPrimary() {
        return nationalityPrimary;
    }

    public void setNationalityPrimary(String nationalityPrimary) {
        this.nationalityPrimary = nationalityPrimary;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }
}
