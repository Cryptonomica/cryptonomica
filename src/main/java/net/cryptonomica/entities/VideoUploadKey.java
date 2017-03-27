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
public class VideoUploadKey {

    private static final Logger LOG = Logger.getLogger(OnlineVerification.class.getName());

    @Id
    private String Id; // VideoUploadKey - ramdom string 33 char .................................1
    @Index
    private User googleUser; //...................................................................2
    @Index
    private Date entityCreated; //................................................................3
    @Index
    private String videoUploadKey; //.............................................................4
    // @Index
    private String uploadedVideoId; //..............................................................5

    /* ---- Constructors */

    public VideoUploadKey() {
        this.entityCreated = new Date();
    }

    public VideoUploadKey(User googleUser, String videoUploadKey) {
        this.Id = videoUploadKey;
        this.googleUser = googleUser;
        this.entityCreated = new Date();
        this.videoUploadKey = videoUploadKey;
        this.uploadedVideoId = null;
    }

    /* --- Getters and Setters */

    public String getId() {
        return Id;
    }

    public User getGoogleUser() {
        return googleUser;
    }

    public Date getEntityCreated() {
        return entityCreated;
    }

    public String getVideoUploadKey() {
        return videoUploadKey;
    }

    public String getUploadedVideoId() {
        return uploadedVideoId;
    }

    public void setUploadedVideoId(String uploadedVideoId) {
        this.uploadedVideoId = uploadedVideoId;
    }
}
