package net.cryptonomica.entities;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
@Entity // -> net.cryptonomica.service.OfyService
@Cache
public class VerificationDocument implements Serializable { // <-- this allows bo be returned in API directly

    // private static final Logger LOG = Logger.getLogger(VerificationDocument.class.getName());

    @Id
    private String Id; // random string 33 char ..................................................1
    @Index
    private User googleUser; //...................................................................2
    @Index
    private Date entityCreated; //................................................................3
    @Index
    private String bucketName; //.................................................................4
    @Index
    private String objectName; //............................................................,....5
    @Index
    private String documentsUploadKey; //.........................................................6
    @Index
    private String fingerprint; //................................................................7
    @Index
    private Boolean hidden; //....................................................................8


    /* ---- Constructors */

    public VerificationDocument() {
        this.entityCreated = new Date();
        this.hidden = false;
    }

    public VerificationDocument(String id,
                                User googleUser,
                                String bucketName,
                                String objectName,
                                String documentsUploadKey,
                                String fingerprint) {
        this.Id = id;
        this.googleUser = googleUser;
        this.entityCreated = new Date();
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.documentsUploadKey = documentsUploadKey;
        this.fingerprint = fingerprint;
        this.hidden = false;
    }

    /* --- Methods */



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

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

}