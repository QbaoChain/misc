package com.aethercoder.misc.qtum.walletTransaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @auther Guo Feiyan
 * @date 2017/11/30 下午4:22
 */
public class UnspentOutput {

    /**
     * address : QfNpqYMjmnCwAu1V5fvxrCaNDrPHRNdQX6
     * txid : fd45045dd2674a97fec15abf6dffc5330fe3ac26426cb48c72f0fa4b09a5eab0
     * outputIndex : 1
     * script : 76a914cded3e26d55fe9c5055982d17870b2861f09173388ac
     * satoshis : 890185800
     * height : 63674
     * isStake : false
     * confirmations : 1704
     */
    private String address;

    private String txid;

    private BigDecimal outputIndex;

    private String script;

    private BigDecimal satoshis;

    private BigDecimal height;

    private boolean isStake;
    private BigDecimal confirmations;

    public boolean isOutputAvailableToPay() {
        if (isStake) {
            return confirmations.longValue() > 500;
        }
        return true;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public BigDecimal getOutputIndex() {
        return outputIndex == null ? new BigDecimal("0") : outputIndex;
    }

    public void setOutputIndex(BigDecimal outputIndex) {
        this.outputIndex = outputIndex;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public BigDecimal getSatoshis() {
        BigDecimal resultValue = new BigDecimal("0");
        if (satoshis.longValue() != 0) {
            resultValue = satoshis.divide(Constants.BIT_COIN);
        }
        return new BigDecimal(CommonUtility.formatQTUMDecimal(satoshis.doubleValue()));
    }

    public void setSatoshis(BigDecimal satoshis) {
        this.satoshis = satoshis;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public boolean isIsStake() {
        return isStake;
    }

    public void setIsStake(boolean isStake) {
        this.isStake = isStake;
    }

    public BigDecimal getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(BigDecimal confirmations) {
        this.confirmations = confirmations;
    }
}
