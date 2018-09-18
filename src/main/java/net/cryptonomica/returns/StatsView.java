package net.cryptonomica.returns;

import java.io.Serializable;

public class StatsView implements Serializable {

    private String date;
    private Integer usersRegistered;
    private Integer keysUploaded;
    private Integer verificationsStarted;
    private Integer documentsUploaded;
    private Integer videosUploaded;
    private Integer paymentsMade;
    private Integer keysVerifiedOnline;
    private Integer keysVerifiedOffline;

    /* ---- Constructors */

    public StatsView() {
    }

    /* ---- Getters and Setters */

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getUsersRegistered() {
        return usersRegistered;
    }

    public void setUsersRegistered(Integer usersRegistered) {
        this.usersRegistered = usersRegistered;
    }

    public Integer getKeysUploaded() {
        return keysUploaded;
    }

    public void setKeysUploaded(Integer keysUploaded) {
        this.keysUploaded = keysUploaded;
    }

    public Integer getVerificationsStarted() {
        return verificationsStarted;
    }

    public void setVerificationsStarted(Integer verificationsStarted) {
        this.verificationsStarted = verificationsStarted;
    }

    public Integer getDocumentsUploaded() {
        return documentsUploaded;
    }

    public void setDocumentsUploaded(Integer documentsUploaded) {
        this.documentsUploaded = documentsUploaded;
    }

    public Integer getVideosUploaded() {
        return videosUploaded;
    }

    public void setVideosUploaded(Integer videosUploaded) {
        this.videosUploaded = videosUploaded;
    }

    public Integer getPaymentsMade() {
        return paymentsMade;
    }

    public void setPaymentsMade(Integer paymentsMade) {
        this.paymentsMade = paymentsMade;
    }

    public Integer getKeysVerifiedOnline() {
        return keysVerifiedOnline;
    }

    public void setKeysVerifiedOnline(Integer keysVerifiedOnline) {
        this.keysVerifiedOnline = keysVerifiedOnline;
    }

    public Integer getKeysVerifiedOffline() {
        return keysVerifiedOffline;
    }

    public void setKeysVerifiedOffline(Integer keysVerifiedOffline) {
        this.keysVerifiedOffline = keysVerifiedOffline;
    }

}
