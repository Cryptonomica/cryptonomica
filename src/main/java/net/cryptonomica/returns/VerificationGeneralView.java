package net.cryptonomica.returns;

import net.cryptonomica.entities.Verification;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
public class VerificationGeneralView implements Serializable {
    Long verificationId;
    Date verifiedOn;
    String verificationInfo;
    String verifiedByCryptonomicaUserId; // websafe string of CryptonomicaUser key

    public VerificationGeneralView() {
    }

    public VerificationGeneralView(Verification verification) {
        this.verificationId = verification.getId();
        this.verifiedOn = verification.getVerifiedOn();
        this.verificationInfo = verification.getVerificationInfo();
        this.verifiedByCryptonomicaUserId = verification.getVerifiedByCryptonomicaUserId();
    } // end of constructor

    public Long getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(Long verificationId) {
        this.verificationId = verificationId;
    }

    public Date getVerifiedOn() {
        return verifiedOn;
    }

    public void setVerifiedOn(Date verifiedOn) {
        this.verifiedOn = verifiedOn;
    }

    public String getVerificationInfo() {
        return verificationInfo;
    }

    public void setVerificationInfo(String verificationInfo) {
        this.verificationInfo = verificationInfo;
    }

    public String getVerifiedByCryptonomicaUserId() {
        return verifiedByCryptonomicaUserId;
    }

    public void setVerifiedByCryptonomicaUserId(String verifiedByCryptonomicaUserId) {
        this.verifiedByCryptonomicaUserId = verifiedByCryptonomicaUserId;
    }
}
