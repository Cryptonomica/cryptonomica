package net.cryptonomica.returns;

import java.io.Serializable;

/**
 *
 */
public class EthAddDocReturn implements Serializable {

    private String sha256; // hash of the document stored, without "0x"
    private String txHash; // transaction that stored the doc

}
