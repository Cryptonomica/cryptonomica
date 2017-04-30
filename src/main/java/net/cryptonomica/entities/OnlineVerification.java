package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Can be verification of: 1) Public PGP key, 2) Licence 3) Company 4) Authority to represent company
 */
@Entity // -> net.cryptonomica.service.OfyService
@Cache
public class OnlineVerification implements Serializable { // -- can be returned to frontend
    /*---------*/
    // private static final Logger LOG = Logger.getLogger(OnlineVerification.class.getName());
    /*-------------*/
    /* Data Fields */
    /* They have to create also a human-readable DB record, thus data can be duplicated to make record self-explaining */
    @Id
    private String Id; // fingerprint ............................1

    // Objectify Key of PGPPublicKeyData
    // @Index // <-- not neded
    private String PGPPublicKeyDataUrlSafeKey; // ................2

    @Index
    private Email userEmail; //...................................3
    @Index
    private String lastName; // ..................................4
    @Index
    private String firstName; // .................................5

    @Index
    // --- for filters in Objectify:
    // .filter("fingerprintStr", fingerprint.toUpperCase())
    // like: 05600EB8208485E6942666E06A7B21E2844C7980
    private String fingerprintStr; //.............................6

    @Index
    // keyID, like [0xE77173E5]
    private String keyID; // .....................................7

    @Index
    private Date entityCreated; //................................8
    // https://github.com/objectify/objectify/wiki/Entities#maps

    @Index
    private Date verifiedOn; // ..................................8

    @Index
    private String verifiedByCryptonomicaUserId; // ......../.....9

    private String verificationNotes; // ........................10

    private Integer step; // ....................................11

    private ArrayList<String> allowedUsers; //...................12


    /* ----- Constructors: */
    public OnlineVerification() {
        // this.paymentsDataWebSafeStringsList = new ArrayList<>();
        this.entityCreated = new Date();
        this.allowedUsers = new ArrayList<>();
        this.step = 0;
    } // end empty args constructor

    /* ---- Methods */
    public boolean addAllowedUser(String cryptonomicaUserId) {
        return this.allowedUsers.add(cryptonomicaUserId);
    }

    /* ----- Getters and Setters:   */

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

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

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public ArrayList<String> getAllowedUsers() {
        return allowedUsers;
    }

    public void setAllowedUsers(ArrayList<String> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }
}
