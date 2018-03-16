package net.cryptonomica.returns;

import net.cryptonomica.entities.PGPPublicKeyData;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 */
public class SearchPGPPublicKeysReturn implements Serializable {
    String messageToUser;
    PGPPublicKeyGeneralView pgpPublicKeyGeneralView;
    ArrayList<PGPPublicKeyGeneralView> pgpPublicKeyGeneralViewList;

    public SearchPGPPublicKeysReturn() {
    }

    public SearchPGPPublicKeysReturn(String messageToUser, ArrayList<PGPPublicKeyData> pgpPublicKeyDataArrayList) {
        this.messageToUser = messageToUser;
        this.pgpPublicKeyGeneralViewList = new ArrayList<>();
        if (pgpPublicKeyDataArrayList != null) {
            for (PGPPublicKeyData pgpPublicKeyData : pgpPublicKeyDataArrayList
                    ) {
                this.pgpPublicKeyGeneralViewList.add(new PGPPublicKeyGeneralView(pgpPublicKeyData));
            }
        }
    } // end of constructor from ArrayList<PGPPublicKeyData>

    public SearchPGPPublicKeysReturn(String messageToUser, PGPPublicKeyData pgpPublicKeyData) {
        this.messageToUser = messageToUser;
        this.pgpPublicKeyGeneralView = new PGPPublicKeyGeneralView(pgpPublicKeyData);
    } // end of constructor from PGPPublicKeyData

    public String getMessageToUser() {
        return messageToUser;
    }

    public void setMessageToUser(String messageToUser) {
        this.messageToUser = messageToUser;
    }

    public PGPPublicKeyGeneralView getPgpPublicKeyGeneralView() {
        return pgpPublicKeyGeneralView;
    }

    public void setPgpPublicKeyGeneralView(PGPPublicKeyGeneralView pgpPublicKeyGeneralView) {
        this.pgpPublicKeyGeneralView = pgpPublicKeyGeneralView;
    }

    public ArrayList<PGPPublicKeyGeneralView> getPgpPublicKeyGeneralViewList() {
        return pgpPublicKeyGeneralViewList;
    }

    public void setPgpPublicKeyGeneralViewList(ArrayList<PGPPublicKeyGeneralView> pgpPublicKeyGeneralViewList) {
        this.pgpPublicKeyGeneralViewList = pgpPublicKeyGeneralViewList;
    }

    // ---------- custom method:
    public void PublicKeyDatasToGeneralViews(ArrayList<PGPPublicKeyData> pgpPublicKeyDataArrayList) {
        this.pgpPublicKeyGeneralViewList = new ArrayList<>();
        if (pgpPublicKeyDataArrayList != null) {
            for (PGPPublicKeyData pgpPublicKeyData : pgpPublicKeyDataArrayList
                    ) {
                this.pgpPublicKeyGeneralViewList.add(new PGPPublicKeyGeneralView(pgpPublicKeyData));
            }
        }
    }
}
