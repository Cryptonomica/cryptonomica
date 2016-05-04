package net.cryptonomica.returns;

import net.cryptonomica.entities.CryptonomicaUser;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * return search result - list of user profiles
 */
public class UserSearchAndViewReturn implements Serializable {
    String messageToUser;
    ArrayList<UserProfileGeneralView> profilesList;

    public UserSearchAndViewReturn() {
    }

    public UserSearchAndViewReturn(String messageToUser, ArrayList<CryptonomicaUser> cryptonomicaUserArrayList) {
        this.messageToUser = messageToUser;
        this.profilesList = new ArrayList<>();
        if (cryptonomicaUserArrayList != null) {
            for (CryptonomicaUser cryptonomicaUser : cryptonomicaUserArrayList
                    ) {
                this.profilesList.add(new UserProfileGeneralView(cryptonomicaUser));
            }
        }
    }

    public UserSearchAndViewReturn(String messageToUser) {
        this.messageToUser = messageToUser;
        this.profilesList = null;
    }

    public String getMessageToUser() {
        return messageToUser;
    }

    public void setMessageToUser(String messageToUser) {
        this.messageToUser = messageToUser;
    }

    public ArrayList<UserProfileGeneralView> getProfilesList() {
        return profilesList;
    }

    public void setProfilesList(ArrayList<UserProfileGeneralView> profilesList) {
        this.profilesList = profilesList;
    }
}
