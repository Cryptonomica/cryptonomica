package net.cryptonomica.forms;

import java.io.Serializable;

/**
 *
 */
public class PGPPublicKeyUploadForm implements Serializable {
    String asciiArmored;

    public PGPPublicKeyUploadForm() {
    }

    public String getAsciiArmored() {
        return asciiArmored;
    }

    public void setAsciiArmored(String asciiArmored) {
        this.asciiArmored = asciiArmored;
    }
}
