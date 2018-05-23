package net.cryptonomica.returns;

import net.cryptonomica.entities.PGPPublicKeyData;

import java.util.ArrayList;

public class ListOfPGPPublicKeyGeneralViews {

    private ArrayList<PGPPublicKeyGeneralView> keys;

    /* --- Constructor */

    public ListOfPGPPublicKeyGeneralViews() {
        this.keys = new ArrayList<>();
    }

    /* --- Methods */
    public Boolean addPGPPublicKeyGeneralView(PGPPublicKeyGeneralView pgpPublicKeyGeneralView) {
        return this.keys.add(pgpPublicKeyGeneralView);
    }

    public Boolean addKey(PGPPublicKeyData pgpPublicKeyData) {
        return this.keys.add(new PGPPublicKeyGeneralView(pgpPublicKeyData));
    }

    /* --- Getters and Setters */

    public ArrayList<PGPPublicKeyGeneralView> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<PGPPublicKeyGeneralView> keys) {
        this.keys = keys;
    }
}
