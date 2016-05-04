package net.cryptonomica.returns;

import net.cryptonomica.entities.Notary;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 */
public class NotaryGeneralView implements Serializable {
    UserProfileGeneralView userProfileGeneralView;
    String notaryID; // the same as cryptonomica user ID
    String notaryInfo;
    ArrayList<NotaryLicenceGeneralView> notaryLicenceGeneralViews;

    public NotaryGeneralView() {
    } //

    public NotaryGeneralView(
            UserProfileGeneralView userProfileGeneralView,
            Notary notary,
            ArrayList<NotaryLicenceGeneralView> notaryLicenceGeneralViewArrayList) {
        this.userProfileGeneralView = userProfileGeneralView;
        this.notaryID = notary.getId();
        this.notaryInfo = notary.getInfo();
        this.notaryLicenceGeneralViews = notaryLicenceGeneralViewArrayList;
    } // end of constructor

    // constructor to show as a list item in search for notaries
    public NotaryGeneralView(
            UserProfileGeneralView userProfileGeneralView,
            Notary notary) {
        this.userProfileGeneralView = userProfileGeneralView;
        this.notaryID = notary.getId();
        this.notaryInfo = notary.getInfo();
    } // end of constructor

    public UserProfileGeneralView getUserProfileGeneralView() {
        return userProfileGeneralView;
    }

    public void setUserProfileGeneralView(UserProfileGeneralView userProfileGeneralView) {
        this.userProfileGeneralView = userProfileGeneralView;
    }

    public String getNotaryID() {
        return notaryID;
    }

    public void setNotaryID(String notaryID) {
        this.notaryID = notaryID;
    }

    public String getNotaryInfo() {
        return notaryInfo;
    }

    public void setNotaryInfo(String notaryInfo) {
        this.notaryInfo = notaryInfo;
    }

    public ArrayList<NotaryLicenceGeneralView> getNotaryLicenceGeneralViews() {
        return notaryLicenceGeneralViews;
    }

    public void setNotaryLicenceGeneralViews(ArrayList<NotaryLicenceGeneralView> notaryLicenceGeneralViews) {
        this.notaryLicenceGeneralViews = notaryLicenceGeneralViews;
    }
}
