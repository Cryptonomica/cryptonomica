package net.cryptonomica.entities;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Text;
import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import org.bouncycastle.openpgp.PGPPublicKey;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

@Entity // -> net.cryptonomica.service.OfyService
// @Cache // --- ? can be turned off to show changes faster
public class PGPPublicKeyData {
    /* ---- Logger */
    private static final Logger LOG = Logger.getLogger(PGPPublicKeyData.class.getName());
    /*------------ */
    // Key owner's key.
    // It makes possible to store the same key to different users - for testing
    @Parent // TODO: should we keep this?
    private Key<CryptonomicaUser> cryptonomicaUserKey; // .....1
    @Id
    // @Id fields cannot be filtered on !!!!
    private String fingerprint; //.............................2
    @Index
    // --- for filters in Objectify:
    // .filter("fingerprintStr", fingerprint.toUpperCase())
    // like: 05600EB8208485E6942666E06A7B21E2844C7980
    private String fingerprintStr; //..........................3
    @Index
    // key of this entity as a website string,
    // can be used easy get a key
    private String webSafeString; //...........................4
    @Index
    // cryptonomicaUserId of the key owner
    // (same as in @Parent )
    private String cryptonomicaUserId; //......................5
    @Index
    // keyID, like [0xE77173E5]
    private String keyID; // ..................................6
    @Index
    // key userID,
    // like: John Silver <jonh.silver@gmail.com>;
    private String userID; //..................................7
    @Index
    private String firstName; //...............................8
    @Index
    private String lastName; //................................9
    @Index
    private Email userEmail; //...............................10
    @Index
    private Date created; //..................................11
    @Index
    private Date exp; //......................................12
    @Index
    private int bitStrength; //...............................13
    private Text asciiArmored; //.............................14
    @Index
    private Boolean verified; //.off-line verification........15
    @Index
    // can be used to filter what to show
    private Boolean active; //................................16
    // verification info can be shown
    // by pressing button "show verification"
    // on the same page, or passed as parameter to
    // show-verification page
    private List<String> verificationsWebSafeStrings; //......17
    @Index
    private Boolean paid; //..................................18
    private Long PaymentDataID; //............................19
    @Index
    private Date entityCreated; //............................20
    @Index
    // if user has a credit card with different spelling
    // of the last and first name
    private String nameOnCard; //.............................21
    @Index
    // onlineVerificationFinished - true when approved by
    // compliance officer
    // OnlineVerification entity ID -> fingerprint
    private Boolean onlineVerificationFinished; //............22
    private Date userBirthday; //.............................23
    //
    // Nationality - from user passport or id document:
    // 2-letter country codes defined in ISO 3166
    // like returned by Locale.getISOCountries() in Java
    // see: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
    // one key can have only one nationality defined
    private String nationality; //............................24

    /* --- Constructors: */
    public PGPPublicKeyData() {
        this.verificationsWebSafeStrings = new ArrayList<>();
        this.entityCreated = new Date();
        this.nameOnCard = null;
        this.verified = false;
        this.onlineVerificationFinished = false;
    } // end: empty constructor

    public PGPPublicKeyData(PGPPublicKey pgpPublicKey, String asciiArmored, String cryptonomicaUserId) {
        // fingerprint
        this.fingerprint = DatatypeConverter.printHexBinary(
                pgpPublicKey.getFingerprint()
        );
        this.fingerprintStr = this.fingerprint; //
        //
        this.cryptonomicaUserKey = Key.create(CryptonomicaUser.class, cryptonomicaUserId);
        this.webSafeString = Key.create(this.cryptonomicaUserKey, PGPPublicKeyData.class, this.fingerprint)
                .toWebSafeString();
        // keyID, like [0xE77173E5]
        StringBuilder keyIDstrBuilder = new StringBuilder();
        keyIDstrBuilder.append("[0x");
        keyIDstrBuilder.append(Integer.toHexString((int) pgpPublicKey.getKeyID()).toUpperCase());
        keyIDstrBuilder.append("]");
        this.keyID = keyIDstrBuilder.toString();

        // userID
        StringBuilder userIDstrBuilder = new StringBuilder();
        Iterator userIDsIterator = pgpPublicKey.getUserIDs(); //
        LOG.warning("Iterator userIDsIterator = pgpPublicKey.getUserIDs(); // then iterate: ");
        int i = 0;
        while (userIDsIterator.hasNext()) {
            String next = userIDsIterator.next().toString();
            userIDstrBuilder.append(next);
            LOG.warning(i++ + " next: " + next);
            userIDstrBuilder.append("; ");
        }

        this.userID = userIDstrBuilder.toString();
        LOG.warning("userID: " + userID);

        // user's first and last name and email:
        List<String> userIdList = Splitter.on(' ').trimResults().omitEmptyStrings().splitToList(userID);
        LOG.warning("List<String> userIdList:");
        for (String s : userIdList) {
            LOG.warning(s);
        }

        try {
            this.firstName = userIdList.get(0);
        } catch (Exception e) {
            LOG.warning("this.firstName = userIdList.get(0); Exception: ");
            LOG.warning(e.getMessage());
        }

        try {
            this.lastName = userIdList.get(1);
        } catch (Exception e) {
            LOG.warning("this.lastName = userIdList.get(1); Exception:");
            LOG.warning(e.getMessage());
        }

        String userEmailDirty = null;
        try {
            userEmailDirty = userIdList.get(userIdList.size() - 1);
        } catch (Exception e) {
            LOG.warning("userEmailDirty = userIdList.get(userIdList.size() - 1); Exception: ");
            LOG.warning(e.getMessage());
        }

        try {
            this.userEmail = new Email(userEmailDirty
                    .substring(
                            1, userEmailDirty.length() - 2
                    )
            );
        } catch (Exception e) {
            LOG.warning("this.userEmail = new Email(userEmailDirty ... Exception: ");
            LOG.warning(e.getMessage());
        }

        // PGP key creation date
        this.created = pgpPublicKey.getCreationTime();
        // PGP key exp date
        this.exp = org.apache.commons.lang3.time.DateUtils.addSeconds(created, (int) pgpPublicKey.getValidSeconds());
        // Bit strength
        this.bitStrength = pgpPublicKey.getBitStrength();
        // ----- Armored
        this.asciiArmored = new Text(asciiArmored);
        this.cryptonomicaUserId = cryptonomicaUserId;
        this.verificationsWebSafeStrings = new ArrayList<>();
        // ---
        this.entityCreated = new Date(); // <- final
        this.nameOnCard = null;

        this.verified = false;
        this.onlineVerificationFinished = false;

    } // end:  costructor with args

    /* ------- Methods */

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void addVerification(String verificationKeyWebSafeString) {
        if (this.verificationsWebSafeStrings == null) {
            this.verificationsWebSafeStrings = new ArrayList<>();
        }
        this.verificationsWebSafeStrings.add(verificationKeyWebSafeString);
    }

    /* ----- Getters and Setters:  */

    public Key<CryptonomicaUser> getCryptonomicaUserKey() {
        return cryptonomicaUserKey;
    }

    public void setCryptonomicaUserKey(Key<CryptonomicaUser> cryptonomicaUserKey) {
        this.cryptonomicaUserKey = cryptonomicaUserKey;
    }

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

    public String getWebSafeString() {
        return webSafeString;
    }

    public void setWebSafeString(String webSafeString) {
        this.webSafeString = webSafeString;
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

    public Email getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(Email userEmail) {
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

    public int getBitStrength() {
        return bitStrength;
    }

    public void setBitStrength(int bitStrength) {
        this.bitStrength = bitStrength;
    }

    public Text getAsciiArmored() {
        return asciiArmored;
    }

    public void setAsciiArmored(Text asciiArmored) {
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

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Long getPaymentDataID() {
        return PaymentDataID;
    }

    public void setPaymentDataID(Long paymentDataID) {
        PaymentDataID = paymentDataID;
    }

    public Date getEntityCreated() {
        return entityCreated;
    }

    public void setEntityCreated(Date entityCreated) {
        this.entityCreated = entityCreated;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    public Boolean getOnlineVerificationFinished() {
        return onlineVerificationFinished;
    }

    public void setOnlineVerificationFinished(Boolean onlineVerificationFinished) {
        this.onlineVerificationFinished = onlineVerificationFinished;
    }

    public Date getUserBirthday() {
        return userBirthday;
    }

    public void setUserBirthday(Date userBirthday) {
        this.userBirthday = userBirthday;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    // end getters and setters
}
