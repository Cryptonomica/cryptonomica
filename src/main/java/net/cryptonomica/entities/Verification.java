package net.cryptonomica.entities;

import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import net.cryptonomica.forms.AddNotaryForm;
import net.cryptonomica.forms.VerifyPGPPublicKeyForm;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Can be verification of: 1) Public PGP key, 2) Licence 3) Company 4) Authority to represent company
 * Online key Verification has separate class: OnlineVerification
 */
@Entity // -> net.cryptonomica.service.OfyService
@Cache
public class Verification {
    /*---------*/
    private static final Logger LOG = Logger.getLogger(Verification.class.getName());
    /*---------*/
    @Id
    private Long Id; // ..........................................1
    // @Index
    private Key verifiedEntityKey; // ............................2
    @Index
    private String verifiedEntityKeyWebSafeString; // ............3
    @Index
    private String verifiedEntityType; // ........................4
    @Index
    private String verifiedByCryptonomicaUserId; // ......../.....5
    @Index
    private Date verifiedOn; // ..................................6
    // @Index
    private ArrayList<String> paymentsDataWebSafeStringsList; // .7
    private String basedOnDocument; // ...........................8
    private String verificationInfo; // ..........................9
    @Index
    private Date entityCreated; //................................10

    /* ----- Constructors: */
    public Verification() {
        // this.paymentsDataWebSafeStringsList = new ArrayList<>();
        this.entityCreated = new Date();
    } // end empty args constructor

    public Verification(  // << Notary verification
                          AddNotaryForm addNotaryForm,
                          CryptonomicaUser verifyingPerson,
                          Key verifiedEntityKey) {
        LOG.warning("creating verification entity" + " from AddNotaryForm");
        // 2
        this.verifiedEntityKey = verifiedEntityKey;
        LOG.warning("verifiedEntityKey: " + this.verifiedEntityKey.toString()); // "Key<?>(" + this.raw + ")"
        // 3
        this.verifiedEntityKeyWebSafeString = verifiedEntityKey.toWebSafeString();
        LOG.warning("verifiedEntityKeyWebSafeString: " + this.verifiedEntityKeyWebSafeString);
        // 4
        this.verifiedEntityType = verifiedEntityKey.getKind();
        LOG.warning("verifiedEntityType: " + verifiedEntityType);
        // 5
        this.verifiedByCryptonomicaUserId = verifyingPerson.getUserId();
        LOG.warning("verifiedByCryptonomicaUserId: " + verifiedByCryptonomicaUserId);
        // 6
        this.verifiedOn = new Date();
        LOG.warning("verifiedOn: " + verifiedOn);
        // 7
        this.paymentsDataWebSafeStringsList = new ArrayList<>();
        LOG.warning("paymentsDataWebSafeStringsList: " + paymentsDataWebSafeStringsList.toString());
        // 8
        this.basedOnDocument = addNotaryForm.getLicenceInfo();
        LOG.warning("basedOnDocument: " + this.basedOnDocument);
        // 9
        this.verificationInfo = addNotaryForm.getVerificationInfo();
        LOG.warning("verificationInfo: " + this.verificationInfo);
        // 10
        this.entityCreated = new Date();
        LOG.warning("entityCreated: " + this.entityCreated);
    } // end of constructor from AddNotaryForm

    public Verification( // << PGP Public Key verification
                         VerifyPGPPublicKeyForm verifyPGPPublicKeyForm,
                         CryptonomicaUser verifyingPerson,
                         Key verifiedEntityKey) {
        LOG.warning("creating verification entity" + " verifyPGPPublicKeyForm");
        // 2
        this.verifiedEntityKey = verifiedEntityKey;
        LOG.warning("verifiedEntityKey: " + this.verifiedEntityKey.toString()); // "Key<?>(" + this.raw + ")"
        // 3
        this.verifiedEntityKeyWebSafeString = verifiedEntityKey.toWebSafeString();
        LOG.warning("verifiedEntityKeyWebSafeString: " + this.verifiedEntityKeyWebSafeString);
        // 4
        this.verifiedEntityType = verifiedEntityKey.getKind();
        LOG.warning("verifiedEntityType: " + verifiedEntityType);
        // 5
        this.verifiedByCryptonomicaUserId = verifyingPerson.getUserId();
        LOG.warning("verifiedByCryptonomicaUserId: " + verifiedByCryptonomicaUserId);
        // 6
        this.verifiedOn = new Date();
        LOG.warning("verifiedOn: " + verifiedOn);
        // 7
        this.paymentsDataWebSafeStringsList = new ArrayList<>();
        LOG.warning("paymentsDataWebSafeStringsList: " + paymentsDataWebSafeStringsList.toString());
        // 8
        this.basedOnDocument = verifyPGPPublicKeyForm.getBasedOnDocument();
        LOG.warning("basedOnDocument: " + this.basedOnDocument);
        // 9
        this.verificationInfo = verifyPGPPublicKeyForm.getVerificationInfo();
        LOG.warning("verificationInfo: " + verificationInfo);
    } // end of constructor from VerifyPGPPublicKeyForm

    /* ----- Methods:  */

    public void addPaymentDataWebSafeString(String paymentDataWebSafeString) {
        this.paymentsDataWebSafeStringsList.add(paymentDataWebSafeString);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    /* ----- Getters and Setters:   */
    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Key getVerifiedEntityKey() {
        return verifiedEntityKey;
    }

    public void setVerifiedEntityKey(Key verifiedEntityKey) {
        this.verifiedEntityKey = verifiedEntityKey;
    }

    public String getVerifiedEntityKeyWebSafeString() {
        return verifiedEntityKeyWebSafeString;
    }

    public void setVerifiedEntityKeyWebSafeString(String verifiedEntityKeyWebSafeString) {
        this.verifiedEntityKeyWebSafeString = verifiedEntityKeyWebSafeString;
    }

    public String getVerifiedEntityType() {
        return verifiedEntityType;
    }

    public void setVerifiedEntityType(String verifiedEntityType) {
        this.verifiedEntityType = verifiedEntityType;
    }

    public String getVerifiedByCryptonomicaUserId() {
        return verifiedByCryptonomicaUserId;
    }

    public void setVerifiedByCryptonomicaUserId(String verifiedByCryptonomicaUserId) {
        this.verifiedByCryptonomicaUserId = verifiedByCryptonomicaUserId;
    }

    public Date getVerifiedOn() {
        return verifiedOn;
    }

    public void setVerifiedOn(Date verifiedOn) {
        this.verifiedOn = verifiedOn;
    }

    public ArrayList<String> getPaymentsDataWebSafeStringsList() {
        return paymentsDataWebSafeStringsList;
    }

    public void setPaymentsDataWebSafeStringsList(ArrayList<String> paymentsDataWebSafeStringsList) {
        this.paymentsDataWebSafeStringsList = paymentsDataWebSafeStringsList;
    }

    public String getBasedOnDocument() {
        return basedOnDocument;
    }

    public void setBasedOnDocument(String basedOnDocument) {
        this.basedOnDocument = basedOnDocument;
    }

    public String getVerificationInfo() {
        return verificationInfo;
    }

    public void setVerificationInfo(String verificationInfo) {
        this.verificationInfo = verificationInfo;
    }

    public Date getEntityCreated() {
        return entityCreated;
    }

    public void setEntityCreated(Date entityCreated) {
        this.entityCreated = entityCreated;
    }
}
