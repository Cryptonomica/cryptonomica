package net.cryptonomica.entities;

import com.google.appengine.api.blobstore.FileInfo;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

/**
 * Data about user picture stored in the Data Store.
 */
@Entity
@Cache
public class ImageData {
    @Parent
    Key<CryptonomicaUser> cryptonomicaUserKey;
    @Id
    String blobKey;

    FileInfo fileInfo; //https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/blobstore/FileInfo
    // ??? can be stored? -- seems yes
    String fileServeServletLink;
    String servingUrl;
    String gsObjectName; //?

    public ImageData() {
    }

    public ImageData(
            Key<CryptonomicaUser> cryptonomicaUserKey,
            String blobKey,
            FileInfo fileInfo,
            String fileServeServletLink,
            String servingUrl,
            String gsObjectName
    ) {
        this.cryptonomicaUserKey = cryptonomicaUserKey;
        this.blobKey = blobKey;
        this.fileInfo = fileInfo;
        this.fileServeServletLink = fileServeServletLink;
        this.servingUrl = servingUrl;
        this.gsObjectName = gsObjectName;
    } // end of counstructor

    public Key<CryptonomicaUser> getCryptonomicaUserKey() {
        return cryptonomicaUserKey;
    }

    public void setCryptonomicaUserKey(Key<CryptonomicaUser> cryptonomicaUserKey) {
        this.cryptonomicaUserKey = cryptonomicaUserKey;
    }

    public String getBlobKey() {
        return blobKey;
    }

    public void setBlobKey(String blobKey) {
        this.blobKey = blobKey;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public String getFileServeServletLink() {
        return fileServeServletLink;
    }

    public void setFileServeServletLink(String fileServeServletLink) {
        this.fileServeServletLink = fileServeServletLink;
    }

    public String getServingUrl() {
        return servingUrl;
    }

    public void setServingUrl(String servingUrl) {
        this.servingUrl = servingUrl;
    }

    public String getGsObjectName() {
        return gsObjectName;
    }

    public void setGsObjectName(String gsObjectName) {
        this.gsObjectName = gsObjectName;
    }
}
