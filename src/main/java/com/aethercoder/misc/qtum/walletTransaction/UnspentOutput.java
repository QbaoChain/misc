package com.aethercoder.misc.qtum.walletTransaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @auther Guo Feiyan
 * @date 2017/11/30 下午4:22
 */
public class UnspentOutput {

    private String address;
    @JsonProperty("tx_hash")
    private String txHash;
    private Integer vout;
    @JsonProperty("txout_scriptPubKey")
    private String txoutScriptPubKey;
    private BigDecimal amount;
    @JsonProperty("block_height")
    private Long blockHeight;
    @JsonProperty("pubkey_hash")
    private String pubkeyHash;
    @JsonProperty("is_stake")
    private Boolean isStake;
    private Integer confirmations;

    public boolean isOutputAvailableToPay(){
        if ( null== isStake){ return true;}
        if(isStake ){
            return confirmations > 500;
        }
        return true;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Integer getVout() {
        return vout;
    }

    public void setVout(Integer vout) {
        this.vout = vout;
    }

    public String getTxoutScriptPubKey() {
        return txoutScriptPubKey;
    }

    public void setTxoutScriptPubKey(String txoutScriptPubKey) {
        this.txoutScriptPubKey = txoutScriptPubKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getPubkeyHash() {
        return pubkeyHash;
    }

    public void setPubkeyHash(String pubkeyHash) {
        this.pubkeyHash = pubkeyHash;
    }

    public Boolean getStake() {
        return isStake;
    }

    public void setStake(Boolean stake) {
        isStake = stake;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(Integer confirmations) {
        this.confirmations = confirmations;
    }
}
