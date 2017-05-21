package net.cryptonomica.returns;

import net.cryptonomica.entities.OnlineVerification;
import net.cryptonomica.entities.VerificationDocument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * representation of OnlineVerification.class without ArrayList<String> allowedUsers (user who get access
 * from key owner should not have access to list of all users who get access from key owner) and without
 * verificationNotes - this field is for staff only.
 * + contains all documents sent by user for verification (Unnecessary documents have be
 * deleted or marked as 'hidden' by staff )
 */
public class OnlineVerificationView implements Serializable {

    private String fingerprint; // fingerprint ..........................1
    private String pgpPublicKeyDataUrlSafeKey; // .......................2
    private String userEmail; //.........................................3
    private String lastName; // .........................................4
    private String firstName; // ........................................5
    private String keyID; // ............................................6
    private Date entityCreated; //.......................................7
    private Date verifiedOn; // .........................................8
    private String verifiedById; // CryptonomicaUserId ..................9
    private String verifiedByFirstNameLastName;//........................10
    // private String verificationNotes; // not public
    // this should be not public:
    // private ArrayList<String> allowedUsers; // String -userId.
    private String verificationVideoId; // ramdom string 33 char.........11
    private ArrayList<String> verificationDocumentsArray; // ............12
    private String phoneNumber; // +972523333333 ........................13
    private Long stripePaymentForKeyVerificationId; //...................14
    private Boolean paymentMade; //......................................15
    private Boolean paymentVerified; //..................................16
    private Boolean onlineVerificationFinished;// .......................17
    private Boolean onlineVerificationDataVerified; // ..................18
    private Boolean termsAccepted;//.....................................19
    private String cryptonomicaUserId; //................................20
    private Date birthday; //............................................21

    /* --- Constructors: */
    public OnlineVerificationView() {
    }

    public OnlineVerificationView(OnlineVerification onlineVerification,
                                  ArrayList<VerificationDocument> verificationDocumentArrayList) {
        this.fingerprint = onlineVerification.getId(); //
        this.pgpPublicKeyDataUrlSafeKey = onlineVerification.getPgpPublicKeyDataUrlSafeKey();
        if (onlineVerification.getUserEmail() != null) {
            this.userEmail = onlineVerification.getUserEmail().getEmail();
        }
        this.lastName = onlineVerification.getLastName();
        this.firstName = onlineVerification.getFirstName();
        this.keyID = onlineVerification.getKeyID();
        this.entityCreated = onlineVerification.getEntityCreated();
        this.verifiedOn = onlineVerification.getVerifiedOn();
        this.verifiedById = onlineVerification.getVerifiedById();
        this.verifiedByFirstNameLastName = onlineVerification.getVerifiedByFirstNameLastName();
        this.verificationVideoId = onlineVerification.getVerificationVideoId();
        this.phoneNumber = onlineVerification.getPhoneNumber();
        this.stripePaymentForKeyVerificationId = onlineVerification.getStripePaymentForKeyVerificationId();
        this.paymentMade = onlineVerification.getPaymentMade();
        this.paymentVerified = onlineVerification.getPaymentVerified();
        this.onlineVerificationFinished = onlineVerification.getOnlineVerificationFinished();
        this.onlineVerificationDataVerified = onlineVerification.getOnlineVerificationDataVerified();
        this.termsAccepted = onlineVerification.getTermsAccepted();
        this.cryptonomicaUserId = onlineVerification.getCryptonomicaUserId();
        this.birthday = onlineVerification.getBirthday();

        this.verificationDocumentsArray = new ArrayList<>();
        if (verificationDocumentArrayList != null && verificationDocumentArrayList.size() > 0)
            for (VerificationDocument doc : verificationDocumentArrayList) {
                String verificationDocumentId = doc.getId().toString();
                this.verificationDocumentsArray.add(verificationDocumentId);
            }
    } // end of constructor;

    /* --- Getters and Setters: */

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getPgpPublicKeyDataUrlSafeKey() {
        return pgpPublicKeyDataUrlSafeKey;
    }

    public void setPgpPublicKeyDataUrlSafeKey(String pgpPublicKeyDataUrlSafeKey) {
        this.pgpPublicKeyDataUrlSafeKey = pgpPublicKeyDataUrlSafeKey;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
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
        this.verifiedByFirstNameLastName = verifiedByFirstNameLastName;
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

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}
