package net.cryptonomica.forms;

import java.io.Serializable;

/**
 * form to store document on Ethereum Blockchain
 */
public class EthAddDocForm implements Serializable {

    private String docText;

    /* ---- Constructors */

    public EthAddDocForm() {
    }

    public EthAddDocForm(String docText) {
        this.docText = docText;
    }

    /* ---- Getters and Setters: */

    public String getDocText() {
        return docText;
    }

    public void setDocText(String docText) {
        this.docText = docText;
    }

}
