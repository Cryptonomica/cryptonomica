package net.cryptonomica.returns;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.Serializable;

public class Web3jTransactionReceiptWrapper implements Serializable {

    private TransactionReceipt transactionReceipt;
    private String stringValue;
    private Integer integerValue;
    private Boolean booleanValue;

    /* ---- Constructor ---- */

    public Web3jTransactionReceiptWrapper() {
    }

    /* ---- Getters and Setters ---- */

    public TransactionReceipt getTransactionReceipt() {
        return transactionReceipt;
    }

    public void setTransactionReceipt(TransactionReceipt transactionReceipt) {
        this.transactionReceipt = transactionReceipt;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

}
