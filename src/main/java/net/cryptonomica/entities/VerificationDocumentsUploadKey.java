package net.cryptonomica.entities;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 */
@Entity // -> net.cryptonomica.service.OfyService
// @Cache // <- not here
public class VerificationDocumentsUploadKey {

    // private static final Logger LOG = Logger.getLogger(VerificationDocumentsUploadKey.class.getName());

    @Id
    private String Id; // VerificationDocumentsUploadKey - random string 33 char .................1
    @Index
    private User googleUser; //...................................................................2
    @Index
    private Date entityCreated; //................................................................3
    @Index
    private String documentsUploadKey; //.........................................................4

    private ArrayList<String> uploadedDocumentIds; //........................................ ....5

    /* ---- Constructors */

    public VerificationDocumentsUploadKey() {
        this.entityCreated = new Date();
    }

    public VerificationDocumentsUploadKey(User googleUser, String documentsUploadKey) {
        this.Id = documentsUploadKey;
        this.googleUser = googleUser;
        this.entityCreated = new Date();
        this.documentsUploadKey = documentsUploadKey;
        this.uploadedDocumentIds = null;
    }

    /* --- Methods */
    public boolean addUploadedDocumentId(String uploadedDocumentId) {
        if (this.uploadedDocumentIds == null) {
            this.uploadedDocumentIds = new ArrayList<>();
        }
        boolean result = this.uploadedDocumentIds.add(uploadedDocumentId);
        return result;
    }

/* --- Getters and Setters */

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public User getGoogleUser() {
        return googleUser;
    }

    public void setGoogleUser(User googleUser) {
        this.googleUser = googleUser;
    }

    public Date getEntityCreated() {
        return entityCreated;
    }

    public void setEntityCreated(Date entityCreated) {
        this.entityCreated = entityCreated;
    }

    public String getDocumentsUploadKey() {
        return documentsUploadKey;
    }

    public void setDocumentsUploadKey(String documentsUploadKey) {
        this.documentsUploadKey = documentsUploadKey;
    }

    public ArrayList<String> getUploadedDocumentIds() {
        return uploadedDocumentIds;
    }

    public void setUploadedDocumentIds(ArrayList<String> uploadedDocumentIds) {
        this.uploadedDocumentIds = uploadedDocumentIds;
    }
}
