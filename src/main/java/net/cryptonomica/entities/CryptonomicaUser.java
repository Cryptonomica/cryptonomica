package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import net.cryptonomica.forms.NewUserRegistrationForm;

import java.util.ArrayList;
import java.util.Date;

/**
 * Entity: user (a human)
 * first name and last name should be stored as lowercase string for searching,
 * see: https://groups.google.com/forum/#!topic/objectify-appengine/q5X3OSICiLM
 * https://code.google.com/p/googleappengine/issues/detail?id=5136
 * http://stackoverflow.com/questions/1658163/case-insensitive-where-clause-in-gql-query-for-stringproperty/1660385
 * http://stackoverflow.com/questions/6201082/case-insensitive-filter-query-with-objectify-google-appengine
 */

@Entity // -> net.cryptonomica.service.OfyService
// @Cache // -- may be should not stored in cache to be able view changes faster
public class CryptonomicaUser {

    /* Logger */
    // private static final Logger LOG = Logger.getLogger(CryptonomicaUser.class.getName());

    // the same as Google user ID ( googleUser.getUserId() )
    // = com.google.appengine.api.users.User (java.lang.String userId)
    @Id
    private String userId; //.....................................................................1
    @Index
    // see: savedCryptonomicaUserKey.toWebSafeString();
    private String webSafeStringKey; //...........................................................2
    @Index
    // ? if user changes firstname
    private String firstName; // .................................................................3
    @Index
    // ? if user changes lastname
    private String lastName; //...................................................................4
    // non indexed:
    private Date birthday; //.....................................................................5
    // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/users/User
    private User googleUser; //...................................................................6
    @Index
    // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Email
    private Email email; // ......................................................................7
    // user provided info
    // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Text
    private Text userInfo; // ....................................................................8
    // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Link
    private Link userCurrentImageLink; // ........................................................9
    private String imageUploadLink; // ...........................................................10
    @Index
    // we use this to connect and uploaded image to CryptonomicaUser entity in DB
    private String imageUploadKey; // ............................................................11
    private ArrayList<Link> oldUserImageLinks; // ................................................12
    // -- for the future
    // https://github.com/objectify/objectify/wiki/Entities#keys
    private Key<ImageData> imageData; //..........................................................13
    // -- for the future:
    private ArrayList<Key<ImageData>> OldPhotos; //...............................................14
    // @Load
    private ArrayList<Key<PGPPublicKeyData>> publicKeys; // ......................................15
    // ArrayList<String> residencies; // country(ies) of residency - ? not sure we need it
    private Key<Invitation> invitedBy; // ........................................................16
    @Ignore // just find by @Parent
    private ArrayList<Key<Invitation>> invitationsSend; //........................................17
    @Ignore // just find by @Parent
    private ArrayList<Login> logins; //...........................................................18
    private Key<Lawyer> lawyerKey; // ............................................................19
    @Index
    private Boolean isLawyer; // .................................................................20
    private Key<Notary> notaryKey; // ............................................................21
    @Index
    private Boolean isNotary; // .................................................................22
    private Key<Arbitrator> arbitratorKey; // ....................................................23
    @Index
    private Boolean isArbitrator; // .............................................................24
    private Key<CryptonomicaOfficer> cryptonomicaOfficerKey; // ..................................25
    @Index
    private Boolean isCryptonomicaOfficer; // ....................................................26
    // companies user can represent
    private ArrayList<Key<AuthorityToRepresent>> canRepresent; //.................................27
    @Index
    // will be shown in search results (// paid for verification)
    private Boolean active; //....................................................................28
    @Index
    // https://stripe.com/docs/api/java#customer_object
    private String stripeCustomerId;//............................................................29
    @Index
    private Date entityCreatedOn; // .............................................................30

    /* --- Constructors: */

    public CryptonomicaUser() {
        this.entityCreatedOn = new Date();
    }

    public CryptonomicaUser(User googleUser,
                            PGPPublicKeyData pgpPublicKeyData,
                            NewUserRegistrationForm newUserRegistrationForm
    ) {
        this.userId = googleUser.getUserId();
        this.webSafeStringKey = Key.create(
                CryptonomicaUser.class, googleUser.getUserId()
        ).toWebSafeString();
        this.firstName = pgpPublicKeyData.getFirstName().toLowerCase();
        this.lastName = pgpPublicKeyData.getLastName().toLowerCase();
        this.birthday = newUserRegistrationForm.getBirthday();
        this.googleUser = googleUser;
        if (googleUser.getEmail() != null) {
            this.email = new Email(googleUser.getEmail().toLowerCase()); // or email = pgpPublicKeyData.getUserEmail()
        }
        if (newUserRegistrationForm.getUserInfo() != null) {
            this.userInfo = new Text(newUserRegistrationForm.getUserInfo());
        }
        this.publicKeys = new ArrayList<>();
        Key<CryptonomicaUser> cryptonomicaUserKey = Key.create(CryptonomicaUser.class, googleUser.getUserId());
        this.publicKeys.add(
                Key.create(
                        cryptonomicaUserKey, PGPPublicKeyData.class, pgpPublicKeyData.getFingerprint()
                )
        );
        this.entityCreatedOn = new Date();
    } // end of constructor from newUserRegistrationForm

    // ------ Getters and Setters:

    // custom:

    public void setImageUploadKeyToNull() {
        this.imageUploadKey = null;
    }

    public void setEmail(Email email) {
        this.email = new Email(email.getEmail().toLowerCase());
    }

    // general:

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWebSafeStringKey() {
        return webSafeStringKey;
    }

    public void setWebSafeStringKey(String webSafeStringKey) {
        this.webSafeStringKey = webSafeStringKey;
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

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public User getGoogleUser() {
        return googleUser;
    }

    public void setGoogleUser(User googleUser) {
        this.googleUser = googleUser;
    }

    public Email getEmail() {
        return email;
    }

    public Text getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Text userInfo) {
        this.userInfo = userInfo;
    }

    public Link getUserCurrentImageLink() {
        return userCurrentImageLink;
    }

    public void setUserCurrentImageLink(Link userCurrentImageLink) {
        this.userCurrentImageLink = userCurrentImageLink;
    }

    public ArrayList<Link> getOldUserImageLinks() {
        return oldUserImageLinks;
    }

    public void setOldUserImageLinks(ArrayList<Link> oldUserImageLinks) {
        this.oldUserImageLinks = oldUserImageLinks;
    }

    public Key<ImageData> getImageData() {
        return imageData;
    }

    public void setImageData(Key<ImageData> imageData) {
        this.imageData = imageData;
    }

    public ArrayList<Key<ImageData>> getOldPhotos() {
        return OldPhotos;
    }

    public void setOldPhotos(ArrayList<Key<ImageData>> oldPhotos) {
        OldPhotos = oldPhotos;
    }

    public ArrayList<Key<PGPPublicKeyData>> getPublicKeys() {
        return publicKeys;
    }

    public void setPublicKeys(ArrayList<Key<PGPPublicKeyData>> publicKeys) {
        this.publicKeys = publicKeys;
    }

    public Key<Invitation> getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(Key<Invitation> invitedBy) {
        this.invitedBy = invitedBy;
    }

    public ArrayList<Key<Invitation>> getInvitationsSend() {
        return invitationsSend;
    }

    public void setInvitationsSend(ArrayList<Key<Invitation>> invitationsSend) {
        this.invitationsSend = invitationsSend;
    }

    public ArrayList<Login> getLogins() {
        return logins;
    }

    public void setLogins(ArrayList<Login> logins) {
        this.logins = logins;
    }

    public Key<Lawyer> getLawyerKey() {
        return lawyerKey;
    }

    public void setLawyerKey(Key<Lawyer> lawyerKey) {
        this.lawyerKey = lawyerKey;
    }

    public Boolean getLawyer() {
        return isLawyer;
    }

    public void setLawyer(Boolean lawyer) {
        isLawyer = lawyer;
    }

    public Key<Notary> getNotaryKey() {
        return notaryKey;
    }

    public void setNotaryKey(Key<Notary> notaryKey) {
        this.notaryKey = notaryKey;
    }

    public Boolean getNotary() {
        return isNotary;
    }

    public void setNotary(Boolean notary) {
        isNotary = notary;
    }

    public Key<Arbitrator> getArbitratorKey() {
        return arbitratorKey;
    }

    public void setArbitratorKey(Key<Arbitrator> arbitratorKey) {
        this.arbitratorKey = arbitratorKey;
    }

    public Boolean getArbitrator() {
        return isArbitrator;
    }

    public void setArbitrator(Boolean arbitrator) {
        isArbitrator = arbitrator;
    }

    public Key<CryptonomicaOfficer> getCryptonomicaOfficerKey() {
        return cryptonomicaOfficerKey;
    }

    public void setCryptonomicaOfficerKey(Key<CryptonomicaOfficer> cryptonomicaOfficerKey) {
        this.cryptonomicaOfficerKey = cryptonomicaOfficerKey;
    }

    public Boolean getCryptonomicaOfficer() {
        return isCryptonomicaOfficer;
    }

    public void setCryptonomicaOfficer(Boolean cryptonomicaOfficer) {
        isCryptonomicaOfficer = cryptonomicaOfficer;
    }

    public ArrayList<Key<AuthorityToRepresent>> getCanRepresent() {
        return canRepresent;
    }

    public void setCanRepresent(ArrayList<Key<AuthorityToRepresent>> canRepresent) {
        this.canRepresent = canRepresent;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getImageUploadLink() {
        return imageUploadLink;
    }

    public void setImageUploadLink(String imageUploadLink) {
        this.imageUploadLink = imageUploadLink;
    }

    public String getImageUploadKey() {
        return imageUploadKey;
    }

    public void setImageUploadKey(String imageUploadKey) {
        this.imageUploadKey = imageUploadKey;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public Date getEntityCreatedOn() {
        return entityCreatedOn;
    }

    public void setEntityCreatedOn(Date entityCreatedOn) {
        this.entityCreatedOn = entityCreatedOn;
    }

}
