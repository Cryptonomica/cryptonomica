package net.cryptonomica.forms;

/**
 *
 */
public class GetDocBySha256Form {

    private String sha256;

    /* ---- Constructors: */

    public GetDocBySha256Form() {
    }

    public GetDocBySha256Form(String sha256) {
        this.sha256 = sha256;
    }

    /* ---- Getters and Setters: */

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }
}
