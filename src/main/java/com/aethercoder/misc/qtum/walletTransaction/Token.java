package com.aethercoder.misc.qtum.walletTransaction;

public class Token extends Contract {

    private float lastBalance = 0;

    public Token(String contractAddress, String templateUiid, Boolean hasBeenCreated, String date, String senderAddress, String contractName) {
        super(contractAddress, templateUiid, hasBeenCreated, date, senderAddress, contractName);
        this.isSubscribe = true;
    }

    public void setLastBalance(float balance){
        this.lastBalance = balance;
    }

    public float getLastBalance(){
        return lastBalance;
    }

    public boolean isSubscribe() {
        return isSubscribe;
    }

    public void setSubscribe(boolean subscribe) {
        isSubscribe = subscribe;
    }

}