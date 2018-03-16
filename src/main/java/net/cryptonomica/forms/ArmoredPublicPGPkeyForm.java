package net.cryptonomica.forms;

import java.io.Serializable;

/**
 * Created by viktor on 06/01/16.
 */
public class ArmoredPublicPGPkeyForm implements Serializable{

    private String ArmoredPublicPGPkeyBlock;

    public ArmoredPublicPGPkeyForm() {
    }

    public String getArmoredPublicPGPkeyBlock() {
        return ArmoredPublicPGPkeyBlock;
    }

    public void setArmoredPublicPGPkeyBlock(String armoredPublicPGPkeyBlock) {
        ArmoredPublicPGPkeyBlock = armoredPublicPGPkeyBlock;
    }
}
