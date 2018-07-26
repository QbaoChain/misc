package com.aethercoder.misc.qtum.walletTransaction;

/**
 * @auther Guo Feiyan
 * @date 2017/11/30 下午4:49
 */
public class DGPInfo {

    private Integer maxblocksize;
    private Integer blockgaslimit;
    private Integer mingasprice;

    public Integer getMaxblocksize() {
        return maxblocksize;
    }

    public void setMaxblocksize(Integer maxblocksize) {
        this.maxblocksize = maxblocksize;
    }

    public Integer getBlockgaslimit() {
        return blockgaslimit;
    }

    public void setBlockgaslimit(Integer blockgaslimit) {
        this.blockgaslimit = blockgaslimit;
    }

    public Integer getMingasprice() {
        return mingasprice;
    }

    public void setMingasprice(Integer mingasprice) {
        this.mingasprice = mingasprice;
    }
}
