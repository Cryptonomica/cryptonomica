package net.cryptonomica.returns;

import java.io.Serializable;

public class VerificationStatsForAdminView implements Serializable {

    private String date;
    private Integer usersRegistered;
    private Integer verificationsTotal;
    private Integer documentsUploaded;
    private Integer videosUploaded;
    private Integer paymentsMade;

    /* ---- Constructors */

    public VerificationStatsForAdminView() {
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

    public Integer getVerificationsTotal() {
        return verificationsTotal;
    }

    public void setVerificationsTotal(Integer verificationsTotal) {
        this.verificationsTotal = verificationsTotal;
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
}
