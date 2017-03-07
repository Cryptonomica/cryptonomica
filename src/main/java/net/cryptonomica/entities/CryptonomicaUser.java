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
import java.util.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(CryptonomicaUser.class.getName());

    // the same as Google user ID ( googleUser.getUserId() )
    // = com.google.appengine.api.users.User (java.lang.String userId)
    @Id
    String userId; //.............................................................1
    @Index
    // see: savedCryptonomicaUserKey.toWebSafeString();
            String webSafeStringKey; //...................................................2
    @Index
    // ? if user changes firstname
            String firstName; // .........................................................3
    @Index
    // ? if user changes lastname
            String lastName;
    // non indexed:
    Date birthday; //.............................................................4
    // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/users/User
    User googleUser; //...........................................................5
    @Index
    // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Email
            Email email; // ..............................................................6
    // user provided info
    // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Text
    Text userInfo; // ............................................................7
    // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Link
    Link userCurrentImageLink; // ................................................8
    String imageUploadLink; // ...................................................9
    @Index
    // we use this to connect and uploaded image to CryptonomicaUser entity in DB
            String imageUploadKey; // ...................................................10
    ArrayList<Link> oldUserImageLinks; // .......................................11
    // -- for the future
    // https://github.com/objectify/objectify/wiki/Entities#keys
    Key<ImageData> imageData; //.................................................12
    // -- for the future:
    ArrayList<Key<ImageData>> OldPhotos; //......................................13
    // @Load
    ArrayList<Key<PGPPublicKeyData>> publicKeys; // .............................14
    // ArrayList<String> residencies; // country(ies) of residency - ? not sure we need it
    Key<Invitation> invitedBy; // ...............................................15
    @Ignore // just find by @Parent
            ArrayList<Key<Invitation>> invitationsSend; //...............................16
    @Ignore // just find by @Parent
            ArrayList<Login> logins; //..................................................17
    Key<Lawyer> lawyerKey; // ...................................................18
    @Index
    Boolean isLawyer; // ........................................................19
    Key<Notary> notaryKey; // ...................................................20
    @Index
    Boolean isNotary; // ........................................................21
    Key<Arbitrator> arbitratorKey; // ...........................................22
    @Index
    Boolean isArbitrator; // ....................................................23
    Key<CryptonomicaOfficer> cryptonomicaOfficerKey; // .........................24
    @Index
    Boolean isCryptonomicaOfficer; // ...........................................25
    // companies user can represent
    ArrayList<Key<AuthorityToRepresent>> canRepresent; //........................26
    @Index
    // will be shown in search results (// paid for verification)
            Boolean active; //...........................................................27

    /* --- Constructors: */

    public CryptonomicaUser() {
    }

    public CryptonomicaUser(User googleUser,
                            PGPPublicKeyData pgpPublicKeyData,
                            NewUserRegistrationForm newUserRegistrationForm) {
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
    } // end of constructor from newUserRegistrationForm

    // ------ Getters and Setters:

    // custom:

    public void setImageUploadKeyToNull() {
        this.imageUploadKey = null;
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

    public void setEmail(Email email) {
        this.email = email;
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

}
