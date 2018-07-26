package com.aethercoder.misc.qtum.walletTransaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @auther Guo Feiyan
 * @date 2017/11/30 下午5:09
 */
public class FeePerKb {
    @JsonProperty("fee_per_kb")
    BigDecimal feePerKb;

    public BigDecimal getFeePerKb() {
        return feePerKb;
    }

    public void setFeePerKb(BigDecimal feePerKb) {
        this.feePerKb = feePerKb;
    }
}
