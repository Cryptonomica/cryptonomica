package net.cryptonomica.returns;

import net.cryptonomica.entities.OnlineVerification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class OnlineVerificationsList implements Serializable {

    private ArrayList<OnlineVerification> onlineVerifications;

    // optional information
    private String info;

    // number of objects in ArrayList<OnlineVerification>
    private Integer counter;

    private Date timestamp;

    /* --- Constructor */

    public OnlineVerificationsList() {
        this.onlineVerifications = new ArrayList<>();
        this.info = null;
        this.counter = 0;
        this.timestamp = new Date();
    }

    /* --- Getters and Setters */

    public ArrayList<OnlineVerification> getOnlineVerifications() {
        return onlineVerifications;
    }

    public void setOnlineVerifications(ArrayList<OnlineVerification> onlineVerifications) {
        this.onlineVerifications = onlineVerifications;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
}
