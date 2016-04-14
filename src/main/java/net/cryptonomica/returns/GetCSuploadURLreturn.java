package net.cryptonomica.returns;

import java.io.Serializable;

/**
 *
 */
public class GetCSuploadURLreturn implements Serializable {

    private String imageUploadUrl;
    private String imageUploadKey;
    private String error;

    public GetCSuploadURLreturn() {
    }

    public GetCSuploadURLreturn(String imageUploadUrl, String imageUploadKey, String error) {
        this.imageUploadUrl = imageUploadUrl;
        this.imageUploadKey = imageUploadKey;
        this.error = error;
    }

    // ---- Getters and Setters:

    public String getImageUploadUrl() {
        return imageUploadUrl;
    }

    public void setImageUploadUrl(String imageUploadUrl) {
        this.imageUploadUrl = imageUploadUrl;
    }

    public String getImageUploadKey() {
        return imageUploadKey;
    }

    public void setImageUploadKey(String imageUploadKey) {
        this.imageUploadKey = imageUploadKey;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
