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
public class VerificationDocument {

    private static final Logger LOG = Logger.getLogger(VerificationDocument.class.getName());

    @Id
    private String Id; // ramdom string 33 char ..................................................1
    @Index
    private User googleUser; //...................................................................2
    @Index
    private Date entityCreated; //................................................................3
    @Index
    private String bucketName; //.................................................................4
    @Index
    private String objectName; //................................................................5
    //
    private String documentsUploadKey; //.........................................................6

    /* ---- Constructors */

    public VerificationDocument() {
        this.entityCreated = new Date();
    }

    public VerificationDocument(String id,
                                User googleUser,
                                String bucketName,
                                String objectName,
                                String videoUploadKey) {
        this.Id = id;
        this.googleUser = googleUser;
        this.entityCreated = new Date();
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.documentsUploadKey = videoUploadKey;
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

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName1) {
        this.objectName = objectName1;
    }

    public String getDocumentsUploadKey() {
        return documentsUploadKey;
    }

    public void setDocumentsUploadKey(String documentsUploadKey) {
        this.documentsUploadKey = documentsUploadKey;
    }
}