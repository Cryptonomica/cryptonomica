package net.cryptonomica.parsers;

/**
 *
 */
public class SampleObj {

    String apikey;
    String publisher; // email
    String text; // text to store

    /* ---- Constructors: */

    public SampleObj() {
    }

    public SampleObj(String apikey, String publisher, String text) {
        this.apikey = apikey;
        this.publisher = publisher;
        this.text = text;
    }
}
