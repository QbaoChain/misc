package com.aethercoder.misc.qtum.walletTransaction;

public class SendRawTransactionResponse {

    private String result;

    public String txid;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    @Override
    public String toString() {
        return "SendRawTransactionResponse{" +
                "result='" + result + '\'' +
                ", txid='" + txid + '\'' +
                '}';
    }
}
