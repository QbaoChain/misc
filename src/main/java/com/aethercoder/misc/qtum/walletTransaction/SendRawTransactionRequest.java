package com.aethercoder.misc.qtum.walletTransaction;

public class SendRawTransactionRequest {

    private String data;
    private Integer allowHighFee;

    public SendRawTransactionRequest(String data, Integer allowHighFee){
        this.data = data;
        this.allowHighFee = allowHighFee;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getAllowHighFee() {
        return allowHighFee;
    }

    public void setAllowHighFee(Integer allowHighFee) {
        this.allowHighFee = allowHighFee;
    }
}
