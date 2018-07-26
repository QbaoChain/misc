package com.aethercoder.misc.qtum.walletTransaction;

public class CallSmartContractRequest {
    private String[] hashes;

    public String[] getHashes() {
        return hashes;
    }

    public void setHashes(String[] hashes) {
        this.hashes = hashes;
    }

    public CallSmartContractRequest(String[] hashes){
        this.hashes = hashes;
    }
}
