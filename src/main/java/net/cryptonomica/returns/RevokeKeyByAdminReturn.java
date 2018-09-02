package net.cryptonomica.returns;

import com.google.appengine.api.urlfetch.HTTPResponse;

import java.io.Serializable;
import java.util.HashMap;

public class RevokeKeyByAdminReturn implements Serializable {

    private PGPPublicKeyAPIView pgpPublicKeyAPIView;
    private HashMap<String, Integer> webhooksResponses;

    /* ---- Constructors */

    public RevokeKeyByAdminReturn() {
        this.webhooksResponses = new HashMap<>();
    }

    public RevokeKeyByAdminReturn(PGPPublicKeyAPIView pgpPublicKeyAPIView) {
        this.pgpPublicKeyAPIView = pgpPublicKeyAPIView;
        this.webhooksResponses = new HashMap<>();
    }

    /* --- Methods */

    public void addResponse(String serviceName, HTTPResponse httpResponse) {
        this.webhooksResponses.put(serviceName, httpResponse.getResponseCode());
    }

    /* ---- Getters and Setters */

    public PGPPublicKeyAPIView getPgpPublicKeyAPIView() {
        return pgpPublicKeyAPIView;
    }

    public void setPgpPublicKeyAPIView(PGPPublicKeyAPIView pgpPublicKeyAPIView) {
        this.pgpPublicKeyAPIView = pgpPublicKeyAPIView;
    }

    public HashMap<String, Integer> getWebhooksResponses() {
        return webhooksResponses;
    }

    public void setWebhooksResponses(HashMap<String, Integer> webhooksResponses) {
        this.webhooksResponses = webhooksResponses;
    }

}
