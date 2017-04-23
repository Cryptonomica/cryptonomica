package net.cryptonomica.entities;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;
import java.util.logging.Logger;

/**
 *
 */
@Entity // -> net.cryptonomica.service.OfyService
@Cache
public class VerificationDocumentUploadKey {

    private static final Logger LOG = Logger.getLogger(VerificationDocumentUploadKey.class.getName());

    @Id
    private String Id; // VerificationDocumentUploadKey - random string 33 char .................1
    @Index
    private User googleUser; //...................................................................2
    @Index
    private Date entityCreated; //................................................................3
    @Index
    private String documentsUploadKey; //.........................................................4
    // @Index
    private String uploadedDocumentId; //........................................................5

    /* ---- Constructors */

    public VerificationDocumentUploadKey() {
        this.entityCreated = new Date();
    }

    public VerificationDocumentUploadKey(String id,
                                         User googleUser,
                                         String documentsUploadKey,
                                         String uploadedDocumentId) {
        this.Id = id;
        this.googleUser = googleUser;
        this.entityCreated = new Date();
        this.documentsUploadKey = documentsUploadKey;
        this.uploadedDocumentId = uploadedDocumentId;
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

    public String getUploadedDocumentId() {
        return uploadedDocumentId;
    }

    public void setUploadedDocumentId(String uploadedDocumentsId) {
        this.uploadedDocumentId = uploadedDocumentsId;
    }
}
