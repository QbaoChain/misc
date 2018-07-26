package com.aethercoder.misc.qtum.walletTransaction;

public class Item {
    private String hash;
    private String output;
    private String gas_used;
    private String excepted;

    public String getGas_used() {
        return gas_used;
    }

    public void setGas_used(String gas_used) {
        this.gas_used = gas_used;
    }

    public String getExcepted() {
        return excepted;
    }

    public void setExcepted(String excepted) {
        this.excepted = excepted;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

}
