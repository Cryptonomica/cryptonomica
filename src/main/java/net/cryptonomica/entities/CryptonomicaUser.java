package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import net.cryptonomica.forms.NewUserRegistrationForm;
import net.cryptonomica.forms.UserRegistrationForm;

import java.util.ArrayList;
import java.util.Date;

/**
 * Entity: user (a human)
 */

@Entity
// @Cache // -- may be should not stored in cache to be able view changes faster
public class CryptonomicaUser {
    /* Logger */

    // = com.google.appengine.api.users.User (java.lang.String userId)
    @Id
    String userId;
    @Index
    String webSafeStringKey; // see: savedCryptonomicaUserKey.toWebSafeString();
    @Index
    String firstName; // ? from invitation // ? if user changes firstname?
    @Index
    String lastName; // ? from invitation // ? if user changes lastname?
    // non indexed:
    Date birthday; //
    User googleUser;    // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/users/User
    @Index
    Email email;        // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Email
    Text userInfo;      // user provided info
    // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Text
    Link userCurrentImageLink; // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Link
    String imageUploadLink;
    @Index
    String imageUploadKey; // we use this to connect and uploaded image to CryptonomicaUser entity in DB
    ArrayList<Link> oldUserImageLinks;
    // -- for the future
    Key<ImageData> imageData; // https://github.com/objectify/objectify/wiki/Entities#keys
    // -- for the future:
    ArrayList<Key<ImageData>> OldPhotos;
    @Load
    ArrayList<Key<PGPPublicKeyData>> publicKeys;
    // ArrayList<String> residencies; // country(ies) of residency - ? not sure we need it
    Key<Invitation> invitedBy; //
    @Ignore // just find be @Parent
            ArrayList<Key<Invitation>> invitationsSend;
    @Ignore // just find by @Parent
            ArrayList<Login> logins;
    Key<Lawyer> lawyerKey;
    @Index
    Boolean isLawyer;
    Key<Notary> notaryKey;
    @Index
    Boolean isNotary;
    Key<Arbitrator> arbitratorKey;
    @Index
    Boolean isArbitrator;
    Key<CryptonomicaOfficer> cryptonomicaOfficerKey;
    @Index
    Boolean isCryptonomicaOfficer;
    ArrayList<Key<AuthorityToRepresent>> canRepresent; // companies user can represent
    @Index
    Boolean active; // will be shown in search results (// paid for verification)

    /* --- Constructors: */

    public CryptonomicaUser() {
    }

    public CryptonomicaUser(User googleUser, UserRegistrationForm userRegistrationForm) {
        this.userId = googleUser.getUserId();
        this.firstName = userRegistrationForm.getFirstName();
        this.lastName = userRegistrationForm.getLastName();
        this.birthday = userRegistrationForm.getBirthday();
        this.googleUser = googleUser;
        this.email = new Email(googleUser.getEmail());
        this.userInfo = new Text(userRegistrationForm.getUserInfo());
    } // constructor from user registration Form

    public CryptonomicaUser(User googleUser,
                            PGPPublicKeyData pgpPublicKeyData,
                            NewUserRegistrationForm newUserRegistrationForm) {
        this.userId = googleUser.getUserId();
        this.webSafeStringKey = Key.create(
                CryptonomicaUser.class, googleUser.getUserId()
        ).toWebSafeString();
        this.firstName = pgpPublicKeyData.getFirstName();
        this.lastName = pgpPublicKeyData.getLastName();
        this.birthday = newUserRegistrationForm.getBirthday();
        this.googleUser = googleUser;
        if (googleUser.getEmail() != null) {
            this.email = new Email(googleUser.getEmail()); // or email = pgpPublicKeyData.getUserEmail()
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
