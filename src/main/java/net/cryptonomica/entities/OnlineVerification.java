package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.google.gson.Gson;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 */
@Entity // -> net.cryptonomica.service.OfyService
// @Cache // << DO NOT CASH: we need fresh info from DB on every verification step
public class OnlineVerification implements Serializable { // -- can be returned to frontend

    // private static final Logger LOG = Logger.getLogger(OnlineVerification.class.getName());

    /* Data Fields */
    @Id
    private String Id; // fingerprint ...................................1
    // Objectify Key of PGPPublicKeyData
    // @Index // <-- not needed
    private String pgpPublicKeyDataUrlSafeKey; // .......................2
    /* They have to create also a human-readable DB record, thus data can be duplicated to make record self-explaining */
    @Index
    private Email userEmail; //..........................................3
    @Index
    private String lastName; // .........................................4
    @Index
    private String firstName; // ........................................5
    @Index
    // keyID, like [0xE77173E5]
    private String keyID; // ............................................6

    /*  --- for filters in Objectify:*/
    // .filter("fingerprintStr", fingerprint.toUpperCase())
    // like: 05600EB8208485E6942666E06A7B21E2844C7980
    @Index
    private String fingerprintStr; //....................................7
    /* --- utility fields: */
    @Index
    private Date entityCreated; //.......................................8
    // https://github.com/objectify/objectify/wiki/Entities#maps
    @Index
    private Date verifiedOn; // .........................................9
    @Index
    private String verifiedById; // CryptonomicaUserId ..................10
    @Index
    private String verifiedByFirstNameLastName; // ......................11
    // @Index
    private String verificationNotes; // ................................12
    /* users that allowed by the key owner to see verification data*/
    // @Index
    private ArrayList<String> allowedUsers; // String -userId............13
    /* verification data: */
    // Online Verification Video:
    @Index
    private String verificationVideoId; // random string 33 char.........14 +
    // Online Verification Documents:
    // @Index
    private ArrayList<String> verificationDocumentsArray; // ............15
    @Index
    private Boolean verificationDocumentsUploaded; //....................16 +
    // Phone
    @Index
    private String phoneNumber; // +972523333333 ........................17 +
    //
    // StripePaymentForKeyVerification @Id private Long id
    @Index
    private Long stripePaymentForKeyVerificationId; //...................18
    @Index
    private Boolean paymentMade; //......................................19
    @Index
    private Boolean paymentVerified; //..................................20 +
    // All:
    @Index
    private Boolean onlineVerificationFinished;// .......................21
    @Index
    private Boolean onlineVerificationDataVerified; // ..................22
    @Index
    private Boolean termsAccepted; // user accepted terms ...............23
    @Index
    private String cryptonomicaUserId;//.................................24
    // @Index
    // private Date birthday; //.........................................25 // removed 2019-01-18
    @Index
    private String nationality; //.......................................26
    // PromoCode
    @Index
    private String promoCode; // ........................................27
    @Index
    private String promoCodeUsed; // ....................................28

    /* ----- Constructors: */
    public OnlineVerification() {
        this.entityCreated = new Date();
        this.termsAccepted = false;
        this.verificationDocumentsArray = new ArrayList<>();
        this.verificationDocumentsUploaded = false;
        //
        this.allowedUsers = new ArrayList<>();

    } // end empty args constructor

    public OnlineVerification(PGPPublicKeyData pgpPublicKeyData) {
        this.Id = pgpPublicKeyData.getFingerprint(); // (@Id for PGPPublicKeyData is 'fingerprint'
        this.fingerprintStr = pgpPublicKeyData.getFingerprint();
        this.pgpPublicKeyDataUrlSafeKey = pgpPublicKeyData.getWebSafeString();
        this.userEmail = pgpPublicKeyData.getUserEmail();
        this.lastName = pgpPublicKeyData.getLastName();
        this.firstName = pgpPublicKeyData.getFirstName();
        this.keyID = pgpPublicKeyData.getKeyID();
        this.cryptonomicaUserId = pgpPublicKeyData.getCryptonomicaUserId();

        // old PGPPublicKeyData entities (created before 22.05.17) do not have this property
        // Birthday was stored in CryptonomicaUser entity only
        /// this.birthday = pgpPublicKeyData.getUserBirthday(); // 2019-01-18 > removed again
        //
        this.entityCreated = new Date(); // <<< ---
        this.allowedUsers = new ArrayList<>();
        this.verificationDocumentsArray = new ArrayList<>();
        this.termsAccepted = false;
        this.nationality = pgpPublicKeyData.getNationality();
    } // end constructor

    /* ---- Methods */


    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public boolean addAllowedUser(String cryptonomicaUserId) {
        return this.allowedUsers.add(cryptonomicaUserId);
    }

    public boolean addVerificationDocument(String verificationDocumentId) {
        Boolean result = this.verificationDocumentsArray.add(verificationDocumentId);
        if (result && this.verificationDocumentsArray.size() == 2) {
            this.verificationDocumentsUploaded = true;
        }
        return result;
    }

    /* ----- Getters and Setters:   */

    // --- custom :

    public void setUserEmail(Email userEmail) {
        this.userEmail = new Email(userEmail.getEmail().toLowerCase());
    }

    // --- [customized] generated:

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getPgpPublicKeyDataUrlSafeKey() {
        return pgpPublicKeyDataUrlSafeKey;
    }

    public void setPgpPublicKeyDataUrlSafeKey(String pgpPublicKeyDataUrlSafeKey) {
        this.pgpPublicKeyDataUrlSafeKey = pgpPublicKeyDataUrlSafeKey;
    }

    public Email getUserEmail() {
        return userEmail;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName.toLowerCase();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName.toLowerCase();
    }

    public String getKeyID() {
        return keyID;
    }

    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    public String getFingerprintStr() {
        return fingerprintStr;
    }

    public void setFingerprintStr(String fingerprintStr) {
        this.fingerprintStr = fingerprintStr.toUpperCase();
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

    public String getVerifiedById() {
        return verifiedById;
    }

    public void setVerifiedById(String verifiedById) {
        this.verifiedById = verifiedById;
    }

    public String getVerifiedByFirstNameLastName() {
        return verifiedByFirstNameLastName;
    }

    public void setVerifiedByFirstNameLastName(String verifiedByFirstNameLastName) {
        this.verifiedByFirstNameLastName = verifiedByFirstNameLastName.toLowerCase();
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }

    public ArrayList<String> getAllowedUsers() {
        return allowedUsers;
    }

    public void setAllowedUsers(ArrayList<String> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }

    public String getVerificationVideoId() {
        return verificationVideoId;
    }

    public void setVerificationVideoId(String verificationVideoId) {
        this.verificationVideoId = verificationVideoId;
    }

    public ArrayList<String> getVerificationDocumentsArray() {
        return verificationDocumentsArray;
    }

    public void setVerificationDocumentsArray(ArrayList<String> verificationDocumentsArray) {
        this.verificationDocumentsArray = verificationDocumentsArray;
    }

    public Boolean getVerificationDocumentsUploaded() {
        return verificationDocumentsUploaded;
    }

    public void setVerificationDocumentsUploaded(Boolean verificationDocumentsUploaded) {
        this.verificationDocumentsUploaded = verificationDocumentsUploaded;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getStripePaymentForKeyVerificationId() {
        return stripePaymentForKeyVerificationId;
    }

    public void setStripePaymentForKeyVerificationId(Long stripePaymentForKeyVerificationId) {
        this.stripePaymentForKeyVerificationId = stripePaymentForKeyVerificationId;
    }

    public Boolean getPaymentMade() {
        return paymentMade;
    }

    public void setPaymentMade(Boolean paymentMade) {
        this.paymentMade = paymentMade;
    }

    public Boolean getPaymentVerified() {
        return paymentVerified;
    }

    public void setPaymentVerified(Boolean paymentVerified) {
        this.paymentVerified = paymentVerified;
    }

    public Boolean getOnlineVerificationFinished() {
        return onlineVerificationFinished;
    }

    public void setOnlineVerificationFinished(Boolean onlineVerificationFinished) {
        this.onlineVerificationFinished = onlineVerificationFinished;
    }

    public Boolean getOnlineVerificationDataVerified() {
        return onlineVerificationDataVerified;
    }

    public void setOnlineVerificationDataVerified(Boolean onlineVerificationDataVerified) {
        this.onlineVerificationDataVerified = onlineVerificationDataVerified;
    }

    public Boolean getTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(Boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }

    public String getCryptonomicaUserId() {
        return cryptonomicaUserId;
    }

    public void setCryptonomicaUserId(String cryptonomicaUserId) {
        this.cryptonomicaUserId = cryptonomicaUserId;
    }
//
//    public Date getBirthday() {
//        return birthday;
//    }
//
//    public void setBirthday(Date birthday) {
//        this.birthday = birthday;
//    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        PGPPublicKeyData.checkNationalityProperty(nationality);
        this.nationality = nationality.toUpperCase();
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getPromoCodeUsed() {
        return promoCodeUsed;
    }

    public void setPromoCodeUsed(String promoCodeUsed) {
        this.promoCodeUsed = promoCodeUsed;
    }

}
